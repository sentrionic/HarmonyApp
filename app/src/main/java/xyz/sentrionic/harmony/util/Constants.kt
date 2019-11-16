package xyz.sentrionic.harmony.util

class Constants {

    companion object {

        const val BASE_URL = "https://sentrionic.xyz/api/"
        const val PASSWORD_RESET_URL: String = "https://sentrionic.xyz/password_reset/"

        const val NETWORK_TIMEOUT = 3000L
        const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
        const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing

        const val PAGINATION_PAGE_SIZE = 10

        const val GALLERY_REQUEST_CODE = 201
        const val PERMISSIONS_REQUEST_READ_STORAGE: Int = 301
        const val CAMERA_REQUEST_CODE: Int = 5
    }
}