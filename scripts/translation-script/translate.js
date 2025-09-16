require('dotenv').config();
const { TranslateClient, TranslateTextCommand } = require("@aws-sdk/client-translate");
const fs = require('fs');
const path = require('path');
const xml2js = require('xml2js');
const amplify = require("aws-amplify");

const region = process.env.AWS_REGION;
const identityPoolId = process.env.IDENTITY_POOL_ID;

amplify.Amplify.configure({
    Auth: {
        identityPoolId,
        region
    }
});

const fetchCredentials = async() => await amplify.Auth.currentUserCredentials();

const langCodeMap = {
    "pt-BR": "pt",
    "zh-CN": "zh-rCN",
    "zh-TW": "zh-rTW",
    "he": "iw"
};

const main = async() => {
    try {
        const credentials = await fetchCredentials();
        if (!credentials) {
            console.error("Failed to fetch credentials. Exiting...");
            return;
        }

        const client = new TranslateClient({
            region,
            credentials
        });

        const languages = ["ar", "de", "es", "fr", "he", "hi", "it", "ja", "ko", "pt-BR", "zh-CN", "zh-TW"];
        const noTranslationTerms = ["Grab", "Amazon"];

        const translateText = async(text, lang, client, noTranslationTerms) => {
            if (!text.trim()) {
                return text;
            }

            const words = text.split(' ');
            const translatedWords = [];

            for (const word of words) {
                const trimmedWord = word.trim();
                const lowerWord = trimmedWord.toLowerCase();
                const isNoTranslationTerm = noTranslationTerms.some(item => lowerWord.includes(item.toLowerCase()));

                if (isNoTranslationTerm) {
                    translatedWords.push(word);
                } else {
                    const params = {
                        Text: trimmedWord,
                        SourceLanguageCode: 'en',
                        TargetLanguageCode: lang,
                    };

                    try {
                        if (trimmedWord === '') {
                            translatedWords.push(word);
                        } else {
                            const data = await client.send(new TranslateTextCommand(params));
                            const translatedWord = data.TranslatedText.trim();
                            if (translatedWord !== "") {
                                let translatedWordWithTags = word.includes("<b>") ?
                                    `<b>${translatedWord}</b>` :
                                    translatedWord;

                                // Add a backslash before the ' sign if not already present
                                if (!translatedWordWithTags.includes("\\'")) {
                                    translatedWordWithTags = translatedWordWithTags
                                        .replace(/\\/g, "\\\\")
                                        .replace(/'/g, "\\'");
                                }

                                translatedWords.push(translatedWordWithTags);
                            } else {
                                translatedWords.push(word);
                            }
                        }
                    } catch (error) {
                        console.error(`Error translating text to ${lang}. ${error}`);
                        return text;
                    }
                }
            }

            return translatedWords.join(' ');
        };

        const xmlFile = path.join(__dirname, '..', '..', 'app', 'src', 'main', 'res', 'values', 'strings.xml');

        // Check if file exists
        if (!fs.existsSync(xmlFile)) {
            console.error('The file does not exist.');
            return;
        }

        const parser = new xml2js.Parser({ explicitArray: false, preserveChildrenOrder: true });

        fs.readFile(xmlFile, 'utf8', async(err, data) => {
            if (err) {
                console.error(`Error reading file from disk: ${err}`);
                return;
            }

            parser.parseString(data, async(parseErr, result) => {
                if (parseErr) {
                    console.error(`Error parsing XML: ${parseErr}`);
                    return;
                }

                const jsonData = result.resources.string;

                for (let lang of languages) {
                    let translatedData = [];

                    for (const item of jsonData) {
                        let translatedItem = {...item };

                        if (item._ !== undefined && item.$.translatable !== "false") {
                            translatedItem._ = await translateText(item._, lang, client, noTranslationTerms);
                        }

                        translatedData.push(translatedItem);
                    }

                    const builder = new xml2js.Builder({ xmldec: false, renderOpts: { pretty: true } });
                    const xmlContent = builder.buildObject({ resources: { string: translatedData } });

                    let langFolder = langCodeMap[lang] || lang;
                    fs.writeFile(path.join("../../app/src/main/res", `values-${langFolder}/string.xml`), xmlContent, 'utf8', (writeErr) => {
                        if (writeErr) {
                            console.error(`Error writing file: ${writeErr}`);
                        } else {
                            console.log(`The ${lang} language has been translated into ${langFolder}`);
                        }
                    });
                }
            });
        });
    } catch (e) {
        console.error(e);
    }
}

main();