package oppen.oppenbok

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import java.lang.String
import java.math.BigInteger
import java.security.MessageDigest
import javax.security.cert.X509Certificate


object AboutDialog {

    fun show(context: Context) {

        val dialog = AppCompatDialog(context, R.style.AppTheme)

        val view = View.inflate(context, R.layout.dialog_about, null)
        dialog.setContentView(view)

        val closeView = view.findViewById<AppCompatImageButton>(R.id.close_tab_dialog)
        closeView.setOnClickListener {
            dialog.dismiss()
        }

        val versionLabel = view.findViewById<TextView>(R.id.version_label)
        versionLabel.text = BuildConfig.VERSION_NAME

        val oppenlabButton = view.findViewById<AppCompatButton>(R.id.oppenlab_button)
        oppenlabButton.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://oppen.digital")
            })
        }

        val signingInfo = view.findViewById<TextView>(R.id.signing_info)

        val pkgInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.signingInfo.apkContentsSigners.forEach { signature ->
                val appCertificate: X509Certificate = X509Certificate.getInstance(signature.toByteArray())

                val md = MessageDigest.getInstance("SHA-256")
                val digest: ByteArray = md.digest(signature.toByteArray())
                val sha256 = String.format("%032x", BigInteger(1, digest)).toUpperCase().replace("..(?!$)", "$0:")

                val regex = "..(?!$)".toRegex()
                val easyRead = regex.replace(sha256, "$0:")

                val parts = arrayOf(easyRead.substring(0, easyRead.length/2), easyRead.substring(easyRead.length/2 + 1))

                signingInfo.append("signing cert public key:\n\n${parts[0]}\n${parts[1]}\n\n")

                signingInfo.append("expires: " + appCertificate.notAfter + "\n\n")// can give you the date & time the cert expires

                signingInfo.append("subject DN: " + appCertificate.subjectDN + "\n\n")// will give you a Principal named "CN=Android Debug,O=Android,C=US" for any debug certificate that hasn't been handcrafted by the developer.
            }
        } else {
            @Suppress("DEPRECATION")
            pkgInfo.signatures.forEach { signature ->
                //todo
            }
        }
        dialog.show()
    }
}