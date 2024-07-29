package campus.tech.kakao.map.data.repository

import campus.tech.kakao.map.data.dao.SavedSearchWordDao
import campus.tech.kakao.map.data.model.SavedSearchWordData
import campus.tech.kakao.map.domain.model.SavedSearchWordDomain
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultSavedSearchWordRepositoryTest {
    private lateinit var savedSearchWordDao: SavedSearchWordDao
    private lateinit var repository: DefaultSavedSearchWordRepository

    @Before
    fun setup() {
        savedSearchWordDao = mockk()
        repository = DefaultSavedSearchWordRepository(savedSearchWordDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testInsertOrUpdateSearchWord() = runTest {
        // Given
        val searchWord =
            SavedSearchWordDomain(
                name = "부산대병원",
                placeId = "1234",
                address = "부산광역시 서구 구덕로 179",
                latitude = 123.456,
                longitude = 12.34,
            )

        // When
        coEvery { savedSearchWordDao.insertOrUpdateSearchWord(searchWord.toSavedSearchWord()) } just Runs
        repository.insertOrUpdateSearchWord(searchWord)

        // Then
        coVerify { savedSearchWordDao.insertOrUpdateSearchWord(searchWord.toSavedSearchWord()) }
    }

    @Test
    fun testGetAllSearchWords() = runTest {
        // Given
        val expectedList =
            listOf(
                SavedSearchWordDomain(
                    name = "부산대병원",
                    placeId = "1234",
                    address = "부산광역시 서구 구덕로 179",
                    latitude = 123.456,
                    longitude = 12.34,
                ),
                SavedSearchWordDomain(
                    name = "부산대학교",
                    placeId = "1235",
                    address = "부산광역시 금정구 부산대학로63번길 2",
                    latitude = 124.567,
                    longitude = 23.45,
                ),
            )

        // When
        coEvery { savedSearchWordDao.getAllSearchWords() } returns expectedList.map { it.toSavedSearchWord() }
        val result = repository.getAllSearchWords()

        // Then
        coVerify { savedSearchWordDao.getAllSearchWords() }
        assert(result.size == 2)
        assert(result.containsAll(expectedList))
    }

    @Test
    fun testDeleteSearchWordById() = runTest {
        // Given
        val idToDelete = 1L

        // When
        coEvery { savedSearchWordDao.deleteSearchWordById(idToDelete) } just Runs
        repository.deleteSearchWordById(idToDelete)

        // Then
        coVerify { savedSearchWordDao.deleteSearchWordById(idToDelete) }
    }

    private fun SavedSearchWordDomain.toSavedSearchWord() = SavedSearchWordData(
        id = this.id,
        name = this.name,
        placeId = this.placeId,
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude,
    )
}