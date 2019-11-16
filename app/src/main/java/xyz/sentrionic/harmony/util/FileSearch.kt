package xyz.sentrionic.harmony.util

import java.io.File
import java.util.*

object FileSearch {

    /**
     * Search a directory and return a list of all **directories** contained inside
     * @param directory
     * @return
     */
    fun getDirectoryPaths(directory: String): ArrayList<String> {
        val pathArray = ArrayList<String>()
        val file = File(directory)
        val listfiles = file.listFiles()

        for (listfile in listfiles!!) {
            if (listfile.isDirectory) {
                pathArray.add(listfile.absolutePath)
            }
        }
        return pathArray
    }

    /**
     * Search a directory and return a list of all **files** contained inside
     * @param directory
     * @return
     */
    fun getFilePaths(directory: String): ArrayList<String> {
        val pathArray = ArrayList<String>()
        val file = File(directory)
        val listfiles = file.listFiles()

        for (listfile in listfiles!!) {
            if (listfile.isFile && isImage(listfile)) {
                pathArray.add(listfile.absolutePath)
            }
        }
        return pathArray
    }

    private fun isImage(file: File): Boolean {
        return file.name.endsWith(".png") || file.name.endsWith(".jpg") || file.name.endsWith(".jpeg")
    }
}
