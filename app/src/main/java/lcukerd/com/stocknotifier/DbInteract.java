package lcukerd.com.stocknotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Programmer on 24-04-2017.
 */

public class DbInteract {

    private eventDBcontract dBcontract;
    private String[] projection = {eventDBcontract.ListofItem.columnID,
            eventDBcontract.ListofItem.columnstock,
            eventDBcontract.ListofItem.columnsym,
            eventDBcontract.ListofItem.columnreqd,
            eventDBcontract.ListofItem.columnchecked
    };
    DbInteract(Context context)
    {
        dBcontract = new eventDBcontract(context);
    }

    public Cursor readtable()
    {
        SQLiteDatabase db = dBcontract.getReadableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,null,null,null,null,eventDBcontract.ListofItem.columnsym+" ASC");
        /*while (cursor.moveToNext())
            Log.d("DbInteract",cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnsym)));
        */
        return cursor;
    }
    public void addSymbol(String symbol)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(eventDBcontract.ListofItem.columnsym,symbol);
        values.put(eventDBcontract.ListofItem.columnchecked,"0");
        db.insert(eventDBcontract.ListofItem.tableName,null,values);
        Log.d("DbInteract","Symbol added");
    }
    public void deleteSym(String symbol)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        db.delete(eventDBcontract.ListofItem.tableName, eventDBcontract.ListofItem.columnsym + " = '" + symbol+"'", null);
        Log.d("DbInteract","deleted "+symbol);
    }
    public void addreqd(String symbol,String reqd)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(eventDBcontract.ListofItem.columnreqd,reqd);
        db.update(eventDBcontract.ListofItem.tableName,values,eventDBcontract.ListofItem.columnsym + " = ?",new String[]{symbol});
        Log.d("DbInteract","Required value added");
    }
    public String readreqd(String symbol)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,eventDBcontract.ListofItem.columnsym + " = ?",new String[]{symbol},null,null,null);
        cursor.moveToFirst();
        Log.d("DbInteract","Required value read");
        return cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnreqd));
    }
    public String readsym(String id)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,eventDBcontract.ListofItem.columnID + " = ?",new String[]{id},null,null,null);
        cursor.moveToFirst();
        Log.d("DbInteract","Required value read");
        return cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnsym));
    }
    public String[] checked()
    {
        SQLiteDatabase db = dBcontract.getReadableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,null,null,null,null,eventDBcontract.ListofItem.columnsym+" ASC");
        String checkedones[] = new String[cursor.getCount()];
        int count=0;
        while (cursor.moveToNext())
        {
            String ischecked = cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnchecked));
            if (ischecked.equals("1"))
            {
                Log.d("DbInteract",cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnsym)));
                checkedones[count] =  cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnID));
                count++;
            }

        }
        String checkreturn[] = new String[count];
        for (int i=0;i<count;i++)
            checkreturn[i] = checkedones[i];

        return checkreturn;
    }
    public Boolean ischecked(String symbol)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        Cursor cursor = db.query(eventDBcontract.ListofItem.tableName,projection,eventDBcontract.ListofItem.columnsym + " = ?",new String[]{symbol},null,null,null);
        cursor.moveToFirst();
        Log.d("DbInteract","Reading checked state");
        if (cursor.getString(cursor.getColumnIndex(eventDBcontract.ListofItem.columnchecked)).equals("1"))
            return true;
        else
            return false;
    }
    public void check(String symbol,String state)
    {
        SQLiteDatabase db = dBcontract.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(eventDBcontract.ListofItem.columnchecked,state);
        db.update(eventDBcontract.ListofItem.tableName,values,eventDBcontract.ListofItem.columnsym + " = ?",new String[]{symbol});
        Log.d("DbInteract","Check added");
    }

}
