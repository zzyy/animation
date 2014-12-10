package com.zy.animationlearn;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends Activity {

    private static final String Intent_Code_Path_Extra = "codepath";
    private static final String Intent_Default_Action = "com.zy.default.main";
    private static final String Intent_Default_Category = "com.zy.default.category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView v_Content = (ListView) findViewById(R.id.content);

        String path = getIntent().getStringExtra(Intent_Code_Path_Extra);

        List<Map<String, Object>> data = getActivities(path);

        v_Content.setAdapter(new SimpleAdapter(this, data, android.R.layout.simple_list_item_1,
                new String[]{"itemName"}, new int[]{android.R.id.text1}));

        v_Content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                Map<String, Object> data = (Map<String, Object>) parent.getItemAtPosition(position);
                Intent intent = (Intent) data.get("itemIntent");
                startActivity(intent);
            }
        });
    }


    private List<Map<String, Object>> getActivities(String prefix){
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (prefix == null) prefix = "";

        Intent intent = new Intent(Intent_Default_Action);
        intent.addCategory(Intent_Default_Category);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);

        if (resolveInfoList == null){
            return results;
        }

        //多层路径时, 用于判断当前路径是否添加过
        HashMap<String, Boolean> entries = new HashMap<String, Boolean>();
        int N = resolveInfoList.size();
        for (int i=0; i < N; i++){
            ResolveInfo resolveInfo = resolveInfoList.get(i);
            String label = resolveInfo.loadLabel(pm).toString();

            String itemName = null;
            Intent itemIntent = new Intent();

            if ("".equals(prefix) || label.startsWith(prefix)){
                String[] prepath = prefix.split("/");
                String[] path = label.split("/");

                if (prepath.length == path.length -1 || ("".equals(prefix) && path.length ==1)){
                    itemName = path[path.length -1];
                    itemIntent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put(itemName, itemIntent);
                    results.add(item);
                }else {
                    itemName = "".equals(prefix)? path[0] : path[prepath.length];
                    if (entries.get(itemName) == null){
                        itemIntent.putExtra(Intent_Code_Path_Extra, prefix + itemName + "/");
                        itemIntent.setClass(this, this.getClass());
                        //相同的路径只需添加一次,
                        entries.put(itemName, true);

                        HashMap<String, Object> item = new HashMap<String, Object>();
                        item.put(itemName, itemIntent);
                        results.add(item);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
