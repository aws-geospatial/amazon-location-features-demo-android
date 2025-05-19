package com.aws.amazonlocation.ui.main.region

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.data.response.RegionResponse
import com.aws.amazonlocation.databinding.FragmentRegionBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.KEY_SELECTED_REGION
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.regionList
import com.aws.amazonlocation.utils.restartApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegionFragment : BaseFragment() {

    private lateinit var adapter: RegionAdapter
    private lateinit var mBinding: FragmentRegionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRegionBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backPress()
        init()
    }

    private fun init() {
        mBinding.apply {
            val mRegionList = arrayListOf<RegionResponse>()
            val selectedRegion = mPreferenceManager.getValue(
                KEY_SELECTED_REGION,
                regionDisplayName[0]
            )
            regionDisplayName.forEach {
                mRegionList.add(RegionResponse(it, selectedRegion == it))
            }
            val nearestRegion = mPreferenceManager.getValue(KEY_NEAREST_REGION, regionList[0])
            mRegionList[0].name = mRegionList[0].name + " - " + nearestRegion
            val layoutManager = LinearLayoutManager(requireContext())
            val languageCode = getLanguageCode(requireContext())
            val isRtl =
                languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
            adapter = RegionAdapter(
                mRegionList,
                isRtl,
                object : RegionAdapter.RegionInterface {
                    override fun click(position: Int) {
                        mRegionList.forEachIndexed { index, _ ->
                            mRegionList[index].isSelected = index == position
                        }
                        regionDisplayName[position].let {
                            mPreferenceManager.setValue(
                                KEY_SELECTED_REGION,
                                it
                            )
                        }
                        adapter.notifyItemRangeChanged(0, mRegionList.size)
                        mLocationProvider.clearCredentials()
                        lifecycleScope.launch {
                            if (!isRunningTest) {
                                delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                                activity?.restartApplication()
                            }
                        }
                    }
                }
            )
            rvRegion.adapter = adapter
            rvRegion.layoutManager = layoutManager
            ivRegion.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
