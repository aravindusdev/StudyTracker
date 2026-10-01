package com.example.studytracker.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = false)
data class SearchResponse(val docs: List<BookDto> = emptyList())

@JsonClass(generateAdapter = false)
data class BookDto(
    val key: String? = null,
    val title: String? = null,
    val author_name: List<String>? = null
)

interface OpenLibraryService {
    @GET("search.json")
    suspend fun studyBooks(
        @Query("q") query: String = "study skills",
        @Query("limit") limit: Int = 6,
        @Query("fields") fields: String = "key,title,author_name"
    ): SearchResponse
}

class OpenLibraryDataSource(private val service: OpenLibraryService) {
    suspend fun fetchBooks(): List<BookDto> = service.studyBooks().docs
}
