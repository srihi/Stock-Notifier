package lcukerd.com.stocknotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Programmer on 24-04-2017.
 */

public class eventDBcontract extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ListofItem.tableName + " (" +
                    ListofItem.columnID + " INTEGER PRIMARY KEY," +
                    ListofItem.columnstock + " TEXT, " +
                    ListofItem.columnsym + " TEXT, " +
                    ListofItem.columnreqd + " REAL );";

    public static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StockReciever.db";

    public eventDBcontract(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("Database","created");
    }
    public void onUpgrade(SQLiteDatabase db,int oldVersion , int newVersion)
    {

    }

    public static class ListofItem
    {
        public static final String tableName = "List_of_Stock",
                columnID="ID",
                columnstock="Name_of_company",
                columnsym="Symbol",
                columnreqd="Required_value";
    }
}
