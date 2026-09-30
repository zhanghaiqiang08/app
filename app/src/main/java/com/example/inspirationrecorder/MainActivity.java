package com.example.inspirationrecorder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import android.app.Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

    private RecordsDbHelper dbHelper;
    private ListView listView;
    private List<Map<String, String>> data;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new RecordsDbHelper(this);

        listView = findViewById(R.id.list_records);
        data = new ArrayList<>();
        adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_record,
                new String[]{"time", "note"},
                new int[]{R.id.tv_time, R.id.tv_note});
        listView.setAdapter(adapter);

        // 点击底部大按钮：记录当前时间
        findViewById(R.id.btn_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("created_at", now);
                values.put("note", "");
                db.insert("records", null, values);
                loadData();
                Toast.makeText(MainActivity.this,
                        "已记录：" + TIME_FORMAT.format(new Date(now)),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 点击某条记录：弹出对话框编辑备注
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditNoteDialog(position);
            }
        });

        // 长按某条记录：确认后删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
                return true;
            }
        });

        loadData();
    }

    private void loadData() {
        data.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, created_at, note FROM records ORDER BY created_at DESC", null);
        while (cursor.moveToNext()) {
            Map<String, String> item = new HashMap<>();
            item.put("id", String.valueOf(cursor.getLong(0)));
            item.put("time", TIME_FORMAT.format(new Date(cursor.getLong(1))));
            String note = cursor.getString(2);
            item.put("note", note == null || note.isEmpty() ? "（点击添加备注）" : note);
            data.add(item);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void showEditNoteDialog(final int position) {
        final long recordId = Long.parseLong(data.get(position).get("id"));
        final String oldNote = data.get(position).get("note");

        final EditText input = new EditText(this);
        if (!oldNote.equals("（点击添加备注）")) {
            input.setText(oldNote);
        }
        input.setHint("请输入备注内容");
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.edit_note_title))
                .setView(input)
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("note", input.getText().toString());
                        db.update("records", values, "id=?", new String[]{String.valueOf(recordId)});
                        loadData();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showDeleteDialog(final int position) {
        final long recordId = Long.parseLong(data.get(position).get("id"));
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_title))
                .setMessage(getString(R.string.delete_message))
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("records", "id=?", new String[]{String.valueOf(recordId)});
                        loadData();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** SQLite 数据库帮助类，存储所有记录 */
    private static class RecordsDbHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "inspiration.db";
        private static final int DB_VERSION = 1;

        RecordsDbHelper(android.content.Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE records ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "created_at INTEGER NOT NULL, "
                    + "note TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 目前只有一版，无需迁移
        }
    }
}
