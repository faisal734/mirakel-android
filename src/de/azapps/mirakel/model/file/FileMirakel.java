/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 *
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package de.azapps.mirakel.model.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.TypedValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.error.ErrorReporter;
import de.azapps.mirakel.helper.error.ErrorType;
import de.azapps.mirakel.model.MirakelInternalContentProvider;
import de.azapps.mirakel.model.task.Task;
import de.azapps.tools.FileUtils;
import de.azapps.tools.Log;

public class FileMirakel extends FileBase {


    private static final String[] allColumns = { ID, NAME, TASK, PATH };
    public static final String cacheDirPath = FileUtils.getMirakelDir()
            + "image_cache";
    public static final File fileCacheDir = new File(cacheDirPath);
    public static final String TABLE = "files";
    private static final String TAG = "FileMirakel";
    private static final Uri URI = MirakelInternalContentProvider.FILE_URI;

    // private static final String TAG = "FileMirakel";

    protected Uri getUri() {
        return URI;
    }

    /**
     * Get all Tasks
     *
     * @return
     */
    public static List<FileMirakel> all() {
        final List<FileMirakel> files = new ArrayList<FileMirakel>();
        final Cursor c = query(URI, allColumns, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            files.add(cursorToFile(c));
            c.moveToNext();
        }
        c.close();
        return files;
    }

    private static FileMirakel cursorToFile(final Cursor c) {
        return new FileMirakel(c.getInt(c.getColumnIndex(ID)), c.getString(c.getColumnIndex(NAME)),
                               Task.get(c.getInt(c.getColumnIndex(TASK))),
                               Uri.parse(c.getString(c.getColumnIndex(PATH))));
    }

    // Static Methods

    public static void destroyForTask(final Task t) {
        final List<FileMirakel> files = getForTask(t);
        MirakelInternalContentProvider.withTransaction(new MirakelInternalContentProvider.DBTransaction() {
            @Override
            public void exec() {
                for (final FileMirakel file : files) {
                    final File destFile = new File(FileMirakel.fileCacheDir,
                                                   file.getId() + ".png");
                    if (destFile.exists()) {
                        destFile.delete();
                    }
                    file.destroy();
                }
            }
        });
    }

    /**
     * Get a Task by id
     *
     * @param id
     * @return
     */
    public static FileMirakel get(final long id) {
        final Cursor cursor = query(URI, allColumns, "_id=" + id, null, null);
        if (cursor.moveToFirst()) {
            final FileMirakel t = cursorToFile(cursor);
            cursor.close();
            return t;
        }
        cursor.close();
        return null;
    }

    public static int getFileCount(final Task t) {
        if (t == null) {
            return 0;
        }
        final Cursor c = query(URI, new String[] {"count(*)"}, "task_id=?", new String[] { "" + t.getId() },
                               null);
        c.moveToFirst();
        final int count = c.getInt(0);
        c.close();
        return count;
    }

    public static List<FileMirakel> getForTask(final Task task) {
        final List<FileMirakel> files = new ArrayList<>();
        final Cursor cursor = query(URI, allColumns, "task_id=" + task.getId(), null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            files.add(cursorToFile(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return files;
    }


    public static FileMirakel newFile(final Context ctx, final Task task,
                                      final Uri uri) {
        if (uri == null) {
            return null;
        }
        InputStream stream;
        try {
            stream = FileUtils.getStreamFromUri(ctx, uri);
        } catch (final FileNotFoundException e1) {
            ErrorReporter.report(ErrorType.FILE_NOT_FOUND);
            return null;
        }
        final String name = FileUtils.getNameFromUri(ctx, uri);
        final FileMirakel newFile = FileMirakel.newFile(task, name, uri);
        Bitmap myBitmap = null;
        try {
            myBitmap = BitmapFactory.decodeStream(stream);
        } catch (final OutOfMemoryError e) {
            ErrorReporter.report(ErrorType.FILE_TO_LARGE_FOR_THUMBNAIL);
        }
        if (myBitmap != null) {
            final float size = TypedValue.applyDimension(
                                   TypedValue.COMPLEX_UNIT_DIP, 150, ctx.getResources()
                                   .getDisplayMetrics());
            myBitmap = Helpers.getScaleImage(myBitmap, size);
            // create directory if not exists
            boolean success = true;
            if (!FileMirakel.fileCacheDir.exists()) {
                success = FileMirakel.fileCacheDir.mkdirs();
            }
            if (success) {
                try {
                    final File destFile = new File(FileMirakel.fileCacheDir,
                                                   newFile.getId() + ".png");
                    final FileOutputStream out = new FileOutputStream(destFile);
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();
                } catch (final Exception e) {
                    Log.e(TAG, "Exception", e);
                }
            }
        }
        return newFile;
    }

    /**
     * Create a new File
     *
     * @param task
     * @param name
     * @param uri
     * @return new File
     */
    public static FileMirakel newFile(final Task task, final String name,
                                      final Uri uri) {
        final FileMirakel m = new FileMirakel(0, name, task, uri);
        return m.create();
    }

    protected FileMirakel(final int id, final String name, final Task task,
                          final Uri uri) {
        super(id, name, task, uri);
    }

    public FileMirakel create() {
        final ContentValues values = getContentValues();
        values.remove("_id");
        final long insertId = insert(URI, values);
        return FileMirakel.get(insertId);
    }

    @Override
    public void destroy() {
        super.destroy();
        new File(fileCacheDir, getId() + ".png").delete();
    }

    public Bitmap getPreview() {
        final File osFile = new File(fileCacheDir, getId() + ".png");
        if (osFile.exists()) {
            return BitmapFactory.decodeFile(osFile.getAbsolutePath());
        }
        return null;
    }

}
