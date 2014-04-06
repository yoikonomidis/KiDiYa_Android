package service.database;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DataProvider extends ContentProvider {
	// database
	private SQLiteHelper database;
	
	// used for the UriMacher
	private static final int LOCATIONS = 10;
	private static final int LOCATION_ID = 20;
	
	private static final String AUTHORITY = "service.database";

	private static final String BASE_PATH = "service";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
		      + "/" + BASE_PATH);
	
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/location";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	private static final String TAG = "DataProvider";
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, LOCATIONS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", LOCATION_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    switch (uriType) {
	    case LOCATIONS:
	    	rowsDeleted = sqlDB.delete(SQLiteHelper.TABLE_LOCATIONS, selection,
	    			selectionArgs);
	    	break;
	    case LOCATION_ID:
	    	String id = uri.getLastPathSegment();
	    	if (TextUtils.isEmpty(selection)) {
	            rowsDeleted = sqlDB.delete(SQLiteHelper.TABLE_LOCATIONS,
	            		SQLiteHelper.COLUMN_ID + "=" + id, 
	            		null);
	    	} else {
	    		rowsDeleted = sqlDB.delete(SQLiteHelper.TABLE_LOCATIONS,
	    				SQLiteHelper.COLUMN_ID + "=" + id 
	    				+ " and " + selection,
	    				selectionArgs);
	    	}
	    	break;
        default:
        	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    long id = 0;
	    switch (uriType) {
	    case LOCATIONS:
	    	id = sqlDB.insert(SQLiteHelper.TABLE_LOCATIONS, null, values);
	    	break;
	    default:
	    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    uri = Uri.parse(CONTENT_URI + "/" + id);
	    getContext().getContentResolver().notifyChange(uri, null);
	    return uri;
	}

	@Override
	public boolean onCreate() {
		database = new SQLiteHelper(getContext());
	    return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // check if the caller has requested a column which does not exists
	    checkColumns(projection);

	    // Set the table
	    queryBuilder.setTables(SQLiteHelper.TABLE_LOCATIONS);

	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    case LOCATIONS:
	    	break;
	    case LOCATION_ID:
	    	// adding the ID to the original query
	    	queryBuilder.appendWhere(SQLiteHelper.COLUMN_ID + "="
	    			+ uri.getLastPathSegment());
	    	break;
	    default:
	    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }

	    SQLiteDatabase db = database.getWritableDatabase();
	    Cursor cursor = queryBuilder.query(db, projection, selection,
	    		selectionArgs, null, null, sortOrder);
	    // make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);

	    return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, 
		String[] selectionArgs) {
		
		int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsUpdated = 0;
	    switch (uriType) {
	    case LOCATIONS:
	    	rowsUpdated = sqlDB.update(SQLiteHelper.TABLE_LOCATIONS, 
	    				values, 
	    				selection,
	    				selectionArgs);
	    	break;
	    case LOCATION_ID:
	        String id = uri.getLastPathSegment();
	        if (TextUtils.isEmpty(selection)) {
	        	rowsUpdated = sqlDB.update(SQLiteHelper.TABLE_LOCATIONS, 
	        			values,
	        			SQLiteHelper.COLUMN_ID + "=" + id, 
	        			null);
	        } else {
	            rowsUpdated = sqlDB.update(SQLiteHelper.TABLE_LOCATIONS, 
	                    values,
	                    SQLiteHelper.COLUMN_ID + "=" + id 
	                    + " and " 
	                    + selection,
	                    selectionArgs);
	        }
	        break;
	    default:
	        throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;    
	}
	
	private void checkColumns(String[] projection) {
		String[] available = { SQLiteHelper.COLUMN_TIMESTAMP,
			SQLiteHelper.COLUMN_LATITUDE, SQLiteHelper.COLUMN_LONGITUDE,
			SQLiteHelper.COLUMN_ID };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
		    // check if all columns which are requested are available
		    if (!availableColumns.containsAll(requestedColumns)) {
		    	throw new IllegalArgumentException("Unknown columns in projection");
		    }
		}
	}
}
