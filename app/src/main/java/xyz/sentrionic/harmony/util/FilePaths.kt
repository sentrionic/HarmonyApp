package xyz.sentrionic.harmony.util

import android.os.Environment

class FilePaths {

    //"storage/emulated/0"
    var ROOT_DIR = Environment.getExternalStorageDirectory().path

    var PICTURES = "$ROOT_DIR/Pictures"
    var CAMERA = "$ROOT_DIR/DCIM/camera"
    var DOWNLOADS = "$ROOT_DIR/Download"

}
