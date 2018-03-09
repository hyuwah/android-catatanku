package io.github.hyuwah.catatanku;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

  SharedPreferences sharedPref;
  SharedPreferences.Editor editor;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    sharedPref = getBaseContext().getSharedPreferences(
        getString(R.string.pref_file_key), Context.MODE_PRIVATE);
    editor = sharedPref.edit();

    View aboutPage = new AboutPage(this)
        .isRTL(false)
        .setImage(R.mipmap.ic_launcher)
        .setDescription("CatatanKu, aplikasi catatan sederhana")
        .addItem(new Element().setTitle("Version 1.0"))
        .addGroup("Connect with developer")
        .addEmail("muhammad.whydn@gmail.com")
        .addWebsite("http://hyuwah.github.io/")
        .addGitHub("hyuwah", "Check other projects on Github")
        .addItem(getCopyRightsElement())
        .create();

    setContentView(aboutPage);
  }

  Element getCopyRightsElement() {
    Element copyRightsElement = new Element();
    final String copyrights = String
        .format("Copyrights © %1$d", Calendar.getInstance().get(Calendar.YEAR));
    copyRightsElement.setTitle(copyrights);
//        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
    copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
    copyRightsElement.setIconNightTint(android.R.color.white);
    copyRightsElement.setGravity(Gravity.CENTER);
    copyRightsElement.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getBaseContext(), copyrights, Toast.LENGTH_SHORT).show();

        boolean debugToggle = sharedPref.getBoolean(getString(R.string.pref_key_isdebug),false);

        editor.putBoolean(getString(R.string.pref_key_isdebug), !debugToggle);
        editor.commit();
      }
    });
    return copyRightsElement;
  }
}
