package com.zu.sweetalbum.activity;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.zu.sweetalbum.R;
import com.zu.sweetalbum.util.CommonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class FileBrowserActivity extends AppCompatActivity {

    private TextView pathTextView;
    private ListView filesListView;
    private Button selectButton;
    private Button cancelButton;
    private String currentPath;



    private FileListAdapter fileListAdapter;

    private LinkedList<Pair<String,Boolean>> filesAndFolders;

    private TextView titleTextView;
    private View titleActionBar;
    private ViewGroup actionBarContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        Intent intent = getIntent();
        if(intent.getStringExtra("path") == null)
        {
            currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else
        {
            File file = new File(intent.getStringExtra("path"));
            if (file.exists())
            {
                currentPath = intent.getStringExtra("path");

            }else
            {
                currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
        initGUI();
    }

    private void initGUI()
    {
        actionBarContainer = (ViewGroup)findViewById(R.id.FileBrowserActivity_actionBar);
        ViewGroup.LayoutParams layoutParams = actionBarContainer.getLayoutParams();
        int statusBarHeight = CommonUtil.getStatusBarHeight();
        layoutParams.height += statusBarHeight;
        actionBarContainer.setLayoutParams(layoutParams);
        actionBarContainer.setPadding(actionBarContainer.getPaddingLeft(), actionBarContainer.getPaddingTop() + statusBarHeight,
                actionBarContainer.getPaddingRight(), actionBarContainer.getPaddingBottom());


        ImageView backArrow = (ImageView)actionBarContainer.findViewById(R.id.MainActivity_ActionBar_slideIndicator);
        backArrow.setImageResource(R.drawable.back_000000_128);

        FrameLayout customArea = (FrameLayout)findViewById(R.id.MainActivity_actionBar_custom_area);
        titleActionBar = getLayoutInflater().inflate(R.layout.text_action_bar, null);
        titleTextView = (TextView)titleActionBar.findViewById(R.id.TextActionBar_textView_albumName);
        titleTextView.setText("选择FTP根目录");
        customArea.addView(titleActionBar);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pathTextView = (TextView)findViewById(R.id.FileBrowserActivity_textView_path);
        filesListView = (ListView)findViewById(R.id.FileBrowserActivity_listView_files);
        selectButton = (Button)findViewById(R.id.FileBrowserActivity_button_select_current);
        cancelButton = (Button)findViewById(R.id.FileBrowserActivity_button_cancel);


        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("selected_path",currentPath);
                setResult(RESULT_OK,intent);
                FileBrowserActivity.this.finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                {
                    backToParent();
                }else
                {
                    Pair<String,Boolean> pair = filesAndFolders.get(position-1);
                    if(!pair.second)
                    {
                        goToSubFolder(pair.first);
                    }
                }
            }
        });

        filesAndFolders = sort(getFilesAndFolders(currentPath));
        fileListAdapter = new FileListAdapter(createListViewData(filesAndFolders));
        filesListView.setAdapter(fileListAdapter);

        pathTextView.setText(currentPath);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                backToParent();
                return true;
            default:
                return super.onKeyDown(keyCode,event);
        }
    }

    private void backToParent()
    {
        String temp = currentPath;
        try{
            currentPath = new File(currentPath).getParent();
        }catch (Exception e)
        {
            e.printStackTrace();
            currentPath = temp;
        }
        if(currentPath == null)
        {
            currentPath = temp;
        }
        pathTextView.setText(currentPath);
        updateFileList();
    }

    private void goToSubFolder(String path)
    {

        currentPath = path;
        pathTextView.setText(currentPath);
        updateFileList();
    }

    private void updateFileList()
    {
        filesAndFolders.clear();
        filesAndFolders = sort(getFilesAndFolders(currentPath));
        fileListAdapter.setData(createListViewData(filesAndFolders));
        fileListAdapter.notifyDataSetChanged();
    }



    private LinkedList<Pair<String,Boolean>> getFilesAndFolders(String path)
    {
        File file = new File(path);
        File[] files = null;
        try{
            files = file.listFiles();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        LinkedList<Pair<String,Boolean>> data = new LinkedList<>();
        if(files != null && files.length != 0)
        {
            for(File f : files)
            {
                Pair<String,Boolean> pair = new Pair<>(f.getAbsolutePath(),f.isFile());
                data.add(pair);
            }
        }
        return data;
    }

    private LinkedList<HashMap<String,String>> createListViewData(LinkedList<Pair<String,Boolean>> data)
    {

        LinkedList<HashMap<String,String>> result = new LinkedList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("file_name","...返回上层文件夹");
        map.put("is_file",new Boolean(false).toString());
        result.addFirst(map);
        if(data != null)
        {
            for(Pair<String,Boolean> pair : data)
            {
                HashMap<String, String> map1 = new HashMap<>();
                map1.put("file_name",getFileNameByPath(pair.first));
                map1.put("is_file",pair.second.toString());
                result.add(map1);
            }
        }
        return result;
    }

    private String getFileNameByPath(String filePath)
    {
        return filePath.substring(filePath.lastIndexOf("/")+1);
    }

    private LinkedList<Pair<String,Boolean>> sort(LinkedList<Pair<String,Boolean>> data)
    {
        if(data == null)
        {
            return data;
        }
        LinkedList<Pair<String,Boolean>> files = new LinkedList<>();
        LinkedList<Pair<String,Boolean>> folders = new LinkedList<>();
        for(Pair<String, Boolean> pair : data)
        {
            if(pair.second)
            {
                if(files.size() == 0)
                {
                    files.add(pair);
                    continue;
                }
                for(int i = 0; i < files.size(); i++)
                {
                    if(i == files.size()-1)
                    {
                        files.add(pair);
                        break;
                    }
                    else if (pair.first.compareTo(files.get(i).first) < 0)
                    {
                        files.add(i,pair);
                        break;

                    }
                }

            }else
            {
                if(folders.size() == 0)
                {
                    folders.add(pair);
                    continue;
                }
                for(int i = 0; i < folders.size(); i++)
                {
                    if(i == folders.size()-1)
                    {
                        folders.add(pair);
                        break;
                    }
                    else if (pair.first.compareTo(folders.get(i).first) < 0)
                    {
                        folders.add(i,pair);
                        break;
                    }
                }
            }
        }
        folders.addAll(files);
        return folders;
    }

    private class FileListAdapter extends BaseAdapter
    {
        LinkedList<HashMap<String,String>> data ;
        public FileListAdapter(LinkedList<HashMap<String,String>> data)
        {
            setData(data);
        }

        public void setData(LinkedList<HashMap<String,String>> data )
        {
            this.data = data;
            if (this.data == null)
            {
                this.data = new LinkedList<>();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHandler viewHandler;
            if(convertView == null)
            {
                view = View.inflate(FileBrowserActivity.this,R.layout.list_item_simple_file,null);
                viewHandler = new ViewHandler();
                viewHandler.fileName = (TextView) view.findViewById(R.id.list_item_simple_file_name);
                viewHandler.fileIcon = (ImageView)view.findViewById(R.id.list_item_simple_file_icon);
                view.setTag(viewHandler);
            }
            else
            {
                view = convertView;
                viewHandler = (ViewHandler) view.getTag();
            }
            viewHandler.fileName.setText(data.get(position).get("file_name"));
            viewHandler.fileIcon.setImageResource((Boolean.parseBoolean(data.get(position).get("is_file")) == true ?
                            R.drawable.file_797979_256 : R.drawable.folder_61d0ff_256));


            return view;
        }

        private class ViewHandler
        {
            public TextView fileName;
            public ImageView fileIcon;
        }
    }


}
