package com.loic.pomplard.gnr

import android.Manifest
import android.os.Environment
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.loic.pomplard.MainActivityPresenter
import com.loic.pomplard.base.BaseFragmentPresenterImpl
import com.loic.pomplard.constants.FilesConstants
import com.loic.pomplard.gnr.models.Gnr
import com.loic.pomplard.utils.DataUtils
import ru.alexbykov.nopermission.PermissionHelper
import java.io.File


class GnrFragmentPresenterImpl(val v: GnrFragmentPresenter.View) : BaseFragmentPresenterImpl<MainActivityPresenter.View, Void>(), GnrFragmentPresenter {

    private val TAG = GnrFragmentPresenterImpl::class.java.getName()
    lateinit var gnrSelected:Gnr

    override fun viewReady(fragment: GnrFragment, gnr:Gnr) {
        this.gnrSelected = gnr
        checkPermission(fragment)
    }

    /**
     * Check if the {@see Manifest.permission.WRITE_EXTERNAL_STORAGE} has been already accepted|refused.
     */
    fun checkPermission(fragment: GnrFragment) {
        val permissionHelper = PermissionHelper(fragment)

        permissionHelper.check(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onSuccess(this::onSuccess)
                .onDenied(this::onDenied)
                .run();
    }

    private fun onSuccess(){
        v.onSuccessPermission()
    }

    private fun onDenied(){
        v.onDeniedPermission()
    }

    /**
     * Retrieve the PDF file from Internet (Firebase) or locally.
     */
    override fun getGnrPdf() {

        val localGnr = DataUtils.getLocalFile(gnrSelected.srcPdf, FilesConstants.PDF_EXTENSION)

        if(!localGnr.exists()) {
            /**
             * Download the PDF file from Firebase
             */
            localGnr.parentFile.mkdirs()
            localGnr.createNewFile()
            val storage = FirebaseStorage.getInstance()
            val storageReference = storage.getReference()
            val pdfFile = storageReference.child(gnrSelected.srcPdf + FilesConstants.PDF_EXTENSION)
            pdfFile.getFile(localGnr).addOnSuccessListener { taskSnapshot: FileDownloadTask.TaskSnapshot? -> v.downloadGnr(localGnr) }
        } else {
            /**
             * Launch the view to open the existing PDF file
             */
            v.downloadGnr(localGnr)
        }

    }
}