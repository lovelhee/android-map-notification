package campus.tech.kakao.map.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import campus.tech.kakao.map.BuildConfig
import campus.tech.kakao.map.model.KakaoMapProductResponse
import campus.tech.kakao.map.model.MapItem
import campus.tech.kakao.map.model.toEntity
import campus.tech.kakao.map.network.RetrofitClient
import campus.tech.kakao.map.repository.MapItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    application: Application,
    private val repository: MapItemRepository
) : AndroidViewModel(application) {

    val keyword = MutableLiveData<String>()

    private val _searchResults = MutableLiveData<List<MapItem>>()
    val searchResults: LiveData<List<MapItem>> get() = _searchResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        keyword.observeForever {
            if (!it.isNullOrEmpty()) {
                searchPlaces(it)
            } else {
                _searchResults.postValue(emptyList())
            }
        }
    }

    fun setKeyword(keyword: String) {
        this.keyword.value = keyword
    }

    fun clearKeyword() {
        keyword.value = ""
    }

    fun searchPlaces(keyword: String) {
        viewModelScope.launch {
            val apiKey = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}"
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.searchPlaces(apiKey, keyword)
                }
                if (response.isSuccessful) {
                    val documents = response.body()?.documents ?: emptyList()
                    val results = documents.map {
                        MapItem(it.place_name, it.address_name, it.category_group_name, it.x.toDouble(), it.y.toDouble())
                    }
                    _searchResults.postValue(results)
                    saveResultsToDatabase(results)
                } else {
                    _searchResults.postValue(emptyList())
                    _errorMessage.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _searchResults.postValue(emptyList())
                _errorMessage.postValue("네트워크 요청 실패: ${e.message}")
            }
        }
    }

    private fun saveResultsToDatabase(results: List<MapItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteAll()
                val entities = results.map { it.toEntity() }
                repository.insertAll(entities)
            }
        }
    }

    fun getSavedMapItems() {
        viewModelScope.launch {
            val savedItems = withContext(Dispatchers.IO) {
                repository.getAllMapItems()
            }
            _searchResults.postValue(savedItems.map {
                MapItem(it.name, it.address, it.category, it.longitude, it.latitude)
            })
        }
    }

    fun clearMapItems() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteAll()
            }
        }
    }
}