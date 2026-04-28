package com.mochen.reader.presentation.navigation

sealed class Screen(val route: String) {
    object Bookshelf : Screen("bookshelf")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    object Reader : Screen("reader/{bookId}/{chapterIndex}") {
        fun createRoute(bookId: Long, chapterIndex: Int = 0) = "reader/$bookId/$chapterIndex"
    }
    object Search : Screen("search/{bookId}") {
        fun createRoute(bookId: Long) = "search/$bookId"
    }
    object WifiTransfer : Screen("wifi_transfer")
    object Bookmarks : Screen("bookmarks/{bookId}") {
        fun createRoute(bookId: Long) = "bookmarks/$bookId"
    }
    object Notes : Screen("notes/{bookId}") {
        fun createRoute(bookId: Long) = "notes/$bookId"
    }
    object GroupManagement : Screen("group_management")
    object Import : Screen("import")
}
