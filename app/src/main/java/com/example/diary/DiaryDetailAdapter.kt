import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.bumptech.glide.Glide
import com.example.diary.DiaryDetailModel
import com.example.diary.DiaryLocationImageDto
import com.example.diary.MultiImageAdapter
import com.example.diary.databinding.DiaryDetailPlaceRecyclerviewBinding
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class DiaryDetailViewHolder(val binding: DiaryDetailPlaceRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root)

class DiaryDetailAdapter(private val itemList: MutableList<DiaryDetailModel>) : RecyclerView.Adapter<DiaryDetailViewHolder>() {
    // Amazon S3 관련 설정
    private val awsAccessKey = "1807222EE827BB41A77C"
    private val awsSecretKey = "E9DC72D2C24094CB2FE00763EF33330FB7948154"
    private val awsCredentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))// YOUR_REGION을 원하는 지역으로 변경하세요
    private lateinit var transferUtility: TransferUtility
    private lateinit var layoutManager: LinearLayoutManager
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryDetailViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DiaryDetailViewHolder(DiaryDetailPlaceRecyclerviewBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: DiaryDetailViewHolder, position: Int) {
        val data = itemList[position]
        Log.d("detail Adapter", "" + data)
        holder.binding.run {
            diaryDetailPlace.text = data.place
            placeContent.text = data.content

            if (data.place == "MEMO") {
                diaryPlaceDate.visibility = View.GONE
                diaryPlaceTime.visibility = View.GONE
            } else {
                diaryPlaceDate.text = "${data.placeDate}"
                diaryPlaceTime.text = "${data.placeStart} ~ ${data.placeEnd}"
            }
        }

        // layoutManager를 설정하는 부분은 onBindViewHolder에서 이루어집니다.
        layoutManager = LinearLayoutManager(holder.binding.root.context)
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(holder.binding.root.context, LinearLayoutManager.HORIZONTAL, true)

        val diaryLocationImageDto1: List<DiaryLocationImageDto>? = data.imageUris

// Initialize the list to store image paths
        val imagePaths = mutableListOf<String>()

// Check if diaryLocationImageDto1 is not null
        diaryLocationImageDto1?.forEach { dto ->
            // Assuming imageData is the property in DiaryLocationImageDto that holds the image path
            dto.imageData?.let { imagePath ->
                imagePaths.add(imagePath)
            }
        }
        // 이미지 경로 리스트 출력
        imagePaths.forEach { imagePath ->
            println(imagePath)
        }
        s3Client.setEndpoint("https://kr.object.ncloudstorage.com")
        // Initialize TransferUtility
        TransferNetworkLossHandler.getInstance(holder.binding.root.context);

        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(holder.binding.root.context)
            .defaultBucket("diary") // S3 버킷 이름을 변경하세요
            .build()
        val uriList = ArrayList<Uri>()
        // 이미지 불러오기 및 표시
        Log.d("diaryDetailAdapter", ""+diaryLocationImageDto1)
        if (data.imageUris != null && data.imageUris!!.isNotEmpty()) {
            // 이미지를 여러 개 표시하기 위해 RecyclerView로 변경
            Log.d("diaryDetailAdapter", ""+data.imageUris)
            if (diaryLocationImageDto1 != null) {
                for (diary in imagePaths) {
                    if (diary != null) {
                        val uri = diary.toUri()
                        uriList.add(uri)

                    }
                }
            }
            downloadAndInitializeAdapter(uriList, holder.binding)
//            val imageAdapter = MultiImageAdapter(uriList as ArrayList<Uri>, holder.binding.root.context)
//            holder.binding.recyclerView.adapter = imageAdapter
//            holder.binding.recyclerView.layoutManager = layoutManager
            Log.d("detailAdapter", "이미지 추가")
        } else {
            // 이미지가 없는 경우, RecyclerView를 숨깁니다.
            holder.binding.recyclerView.visibility = View.GONE
            Log.d("detailAdapter", "이미지 없음")
        }
    }

    fun updateData(newItems: List<DiaryDetailModel>) {
        if (newItems.isNotEmpty()) { // 리스트가 비어있지 않은 경우에만 업데이트
            itemList.clear()
            itemList.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    private fun downloadAndInitializeAdapter(imageUri: List<Uri>, binding: DiaryDetailPlaceRecyclerviewBinding) {
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
                            val imageAdapter = MultiImageAdapter(downloadFiles, binding.root.context)
                            binding.recyclerView.adapter = imageAdapter
                            //binding.recyclerView.layoutManager = layoutManager
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
