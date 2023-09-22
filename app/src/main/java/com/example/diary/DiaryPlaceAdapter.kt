package com.example.diary

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.example.diary.databinding.DiaryDetailPlaceRecyclerviewBinding
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class DiaryPlaceAdapter (private val itemList: MutableList<DiaryPlaceModel>, val num: Int) :
    RecyclerView.Adapter<DiaryPlaceAdapter.ViewHolder>() {
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(
        awsCredentials,
        Region.getRegion(Regions.AP_NORTHEAST_2)
    )// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility

    companion object {
        private const val ITEM_TYPE_NORMAL = 0
        private const val ITEM_TYPE_MEMO = 1
    }

    interface ItemClickListener {
        fun itemClicked()
    }

    private var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DiaryDetailPlaceRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    inner class ViewHolder(private val binding: DiaryDetailPlaceRecyclerviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.diaryCardView.setOnClickListener {
                val item = itemList[adapterPosition] // 클릭된 아이템
                val intent = Intent(itemView.context, AddPlaceInDiaryActivity::class.java)
                Log.d("어댑터", "" + adapterPosition)
                intent.putExtra("itemPosition", adapterPosition) // position 전달
                intent.putExtra("place", item.place)
                intent.putExtra("date", item.placeDate)
                intent.putExtra("timeStart", item.placeTimeS)
                intent.putExtra("timeEnd", item.placeTimeE)
                intent.putExtra("content", item.content)
                intent.putExtra("address", item.address)
                intent.putExtra("x", item.x)
                intent.putExtra("y", item.y)
                intent.putParcelableArrayListExtra("imageUris", item.imageUris)
                intent.putExtra("new", num)

                itemClickListener?.itemClicked() // 기존 내용 뷰모델에 저장해라 호출

                Log.d(
                    "mylog", "여행지 정보 in 지도" + adapterPosition + item.place + item.placeDate
                            + item.placeTimeS + item.placeTimeE + item.content + item.address + item.x + item.y
                )
                AddDiaryActivity.addContentActivityResult.launch(intent)
            }
        }

        fun bind(item: DiaryPlaceModel) {
            // 데이터를 레이아웃의 뷰에 바인딩
            //Log.d(TAG, "Item content: ${item.content}")
            if (item.place != null) {
                binding.diaryDetailPlace.text = item.place

                if (item.place == "MEMO") {
                    binding.diaryPlaceDate.visibility = View.GONE
                    binding.diaryPlaceTime.visibility = View.GONE
                } else {
                    binding.diaryPlaceDate.text = item.placeDate
                    binding.diaryPlaceTime.text = "${item.placeTimeS} ~ ${item.placeTimeE}"
                }
            }

            binding.placeContent.text = item.content ?: "클릭하여 여행지별 일기를 기록하세요."

            //만약 작성 중인 상태였다면
            if (num == 0) {
                // 이미지 리사이클러뷰 초기화 및 어댑터 연결
                val imageRecyclerView = binding.recyclerView
                imageRecyclerView.layoutManager =
                    LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, true)

                // 이미지 어댑터 초기화 및 연결
                val imageAdapter =
                    MultiImageAdapter(item.imageUris ?: ArrayList(), binding.root.context)
                imageRecyclerView.adapter = imageAdapter
            } else if (num == 1) {
                Log.d("diaryplaceadapter", "수정 중")
                //수정 중인 상태라면
                // layoutManager를 설정하는 부분은 onBindViewHolder에서 이루어집니다.
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    true
                )
                s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
                // Initialize TransferUtility
                TransferNetworkLossHandler.getInstance(binding.root.context);

                transferUtility = TransferUtility.builder()
                    .s3Client(s3Client)
                    .context(binding.root.context)
                    .defaultBucket("diary") // S3 버킷 이름을 변경하세요
                    .build()
                Log.d("DiaryPlaceAdapter", "addimageUris" + item.addimageUris)
                if (item.addimageUris != null) {
                    item.imageUris?.let {
                        downloadAndInitializeAdapter(
                            it,
                            binding,
                            item.imageUris!!,
                            item.addimageUris
                        )
                    }
                } else {
                    item.imageUris?.let {
                        downloadAndInitializeAdapter(it, binding, item.imageUris!!, null)
                    }

//            val imageAdapter = MultiImageAdapter(uriList as ArrayList<Uri>, holder.binding.root.context)
//            holder.binding.recyclerView.adapter = imageAdapter
//            holder.binding.recyclerView.layoutManager = layoutManager
                    Log.d("detailAdapter", "이미지 추가")
                }
            }
        }
    }

    // 특정 아이템을 맨 마지막으로 이동시키는 함수
    fun moveMemoItemToLast() {
        val memoItem = itemList.find { it.place == "MEMO" }
        memoItem?.let {
            itemList.remove(it)
            itemList.add(it)
            notifyDataSetChanged()
        }
    }

    // 데이터를 업데이트하는 메서드
    fun updateData(newItems: List<DiaryPlaceModel>) {
        if (newItems.isNotEmpty()) { // 리스트가 비어있지 않은 경우에만 업데이트
            itemList.clear()
            itemList.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if ("MEMO" == itemList[position].place) {
            return ITEM_TYPE_MEMO
        } else {
            return ITEM_TYPE_NORMAL
        }
    }

    fun setItemClickListener(listener: DiaryPlaceAdapter.ItemClickListener) {
        this.itemClickListener = listener
    }

    private fun downloadAndInitializeAdapter(
        imageUri: List<Uri>,
        binding: DiaryDetailPlaceRecyclerviewBinding,
        imageUris: ArrayList<Uri>,
        addimageUris: ArrayList<Uri>?

    ) {
        // 다운로드할 파일 경로
        val downloadFile = File(binding.root.context.cacheDir, "downloaded_image")

        val transferObserverList = mutableListOf<TransferObserver>()

        val downloadFiles = ArrayList<Uri>() // 이미지 다운로드를 위한 파일 리스트

        for (uri in imageUri) {
            val fileName = uri.lastPathSegment // 파일 이름을 가져옴
            val downloadFile = File(binding.root.context.cacheDir, fileName)
            downloadFiles.add(downloadFile.toUri()) // 파일을 리스트에 추가

            val transferObserver = transferUtility.download(
                "diary",
                uri.toString(),
                downloadFile
            )

            transferObserverList.add(transferObserver)
        }
        // 모든 이미지 다운로드 완료를 기다림
        val completionCount = AtomicInteger(0)

        transferObserverList.forEach { transferObserver ->
            transferObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        completionCount.incrementAndGet()

                        // 모든 이미지가 다운로드 완료되면 어댑터 초기화
                        if (completionCount.get() == imageUri.size) {
                            if(addimageUris != null) {
                                downloadFiles.addAll(addimageUris)
                            }
                            Log.d("DiaryPlaceAdapter", "downloadFiles" + downloadFiles)
                            val imageAdapter =
                                MultiImageAdapter(downloadFiles, binding.root.context)
                            binding.recyclerView.adapter = imageAdapter
                            Log.d("detailAdapter", "이미지 추가")
                        }
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    // 진행 상태 업데이트
                }

                override fun onError(id: Int, ex: Exception) {
                    Log.e("DiaryDetailAdapter", "이미지 다운로드 오류: $ex")
                }
            })
        }
    }
}