package io.github.hyuwah.catatanku.editor;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.styles.Github;
import io.github.hyuwah.catatanku.R;

public class EditorMarkdownActivity extends AppCompatActivity {

  MarkdownView markdownView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor_markdown);

    markdownView = findViewById(R.id.editor_markdown_view);

    Bundle bundle = getIntent().getExtras();
    String mTitle = bundle.getString("TITLE");
    String mBody = bundle.getString("BODY");

    // TODO make bundle into variable, handle lifecycle change

    // TODO Handle Empty view?
    if(TextUtils.isEmpty(mTitle)){
      setTitle("Untitled");
    }else{
      setTitle(mTitle);
    }

    markdownView.addStyleSheet(new Github());
    markdownView.loadMarkdown(bundle.getString("BODY"));

  }
}
