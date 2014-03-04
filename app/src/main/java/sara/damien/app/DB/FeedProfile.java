package sara.damien.app.DB;

import android.provider.BaseColumns;

/**
 * Created by Damien on 01/03/2014.
 */
public class FeedProfile {
    public FeedProfile(){}

    public static abstract class FeedEntry implements BaseColumns{
        public static final String TABLE_NAME = "profile";
        public static final String COLUMN_NAME_LAST_NAME = "Last_Name";
        public static final String COLUMN_NAME_FIRST_NAME = "First_Name";
        public static final String COLUMN_NAME_LAST_SUBJECT = "Last_Subject";
        public static final String COLUMN_NAME_LOC_X = "Loc_X";
        public static final String COLUMN_NAME_LOC_Y = "Loc_Y";
        public static final String COLUMN_NAME_COMPANY = "Company";
        public static final String COLUMN_NAME_EXP_YEARS = "Exp_Years";
        public static final String COLUMN_NAME_SUM_GRADE = "Sum_Grade";
        public static final String COLUMN_NAME_NUMBER_GRADE = "Number_Grade";
        public static final String COLUMN_NAME_STATE = "State";
        public static final String COLUMN_NAME_PICTURE = "Picture";
    }


}
