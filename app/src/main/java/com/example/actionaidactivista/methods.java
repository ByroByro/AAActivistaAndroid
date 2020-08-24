package com.example.actionaidactivista;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.example.actionaidactivista.logic.DownloadProgress;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class methods{

    public static void showAlert(String title, String Message, Context context) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setTitle(title);
            builder.setMessage(Message);
            builder.setPositiveButton("Ok",
                    (dialog, which) -> dialog.cancel());
            builder.show();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void showRequestFailedDialog(Context context){
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setTitle("Request Failed");
            builder.setMessage("Check your internet connection.");
            builder.setPositiveButton("Ok",
                    (dialog, which) -> dialog.cancel());
            builder.show();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDatev2() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static int currentYear(Context ctx) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String s = dateFormat.format(date);
        String[] tokens = s.split("-");
        int year = Integer.parseInt(tokens[0]);
        return year;
    }

    public static boolean checkAge(String dateofbirth, Context ctx) {
        boolean overAge = false;
        try {
            String[] tokens = dateofbirth.split("-");
            int yearOfBirth = Integer.valueOf(tokens[2]);
            int res = currentYear(ctx) - yearOfBirth;
            if (res > 35) {
                overAge = true;
            } else {
                overAge = false;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return overAge;
    }

    public static String getAge(String dateofbirth, Context ctx) {
        String age = "none";
        try {
            String[] tokens = dateofbirth.split(" ");
            String[] tkns = tokens[0].split("/");
            int a = currentYear(ctx) - Integer.parseInt(tkns[2]);
            age = String.valueOf(a);
        } catch (Exception e) {
            System.out.println(e);
        }
        return age;
    }

    public static String changeDateFormat(String date) {
        String newformat = "";
        try {
            String[] tokens = date.split("-");
            newformat = tokens[2] + "-" + tokens[1] + "-" + tokens[0];
        } catch (Exception e) {
            System.out.println(e);
        }
        return newformat;
    }

    public static String removeQoutes(String result) {
        String withoutQoutes = "";
        try {
            if (result.length() >= 2 && result.charAt(0) == '"' && result.charAt(result.length() - 1) == '"') {
                withoutQoutes = result.substring(1, result.length() - 1);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return withoutQoutes;
    }

    public static void showDialog(Dialog mdialog, String message, boolean action) {
        try {
            if (action) {
                mdialog.setContentView(R.layout.custom_progress_dialog);
                final TextView textView = (TextView) mdialog.findViewById(R.id.custom_dialog_text);
                textView.setText(message);
                mdialog.setCanceledOnTouchOutside(false);
                mdialog.setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                        }
                        return true;
                    }
                    return false;
                });
                mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mdialog.show();
            } else {
                mdialog.dismiss();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String getDateForSqlServer() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getFileNameFromUrl(String url, Context context) {
        String name = "";
        try {
            name = url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) {
            Toast.makeText(context, "Error getting filename.", Toast.LENGTH_SHORT).show();
        }
        return name;
    }

    public static String getExtensionFromUrl(String url, Context ctx) {
        String name = "";
        try {
            name = url.substring(url.lastIndexOf('.') + 1);
        } catch (Exception e) {
            Toast.makeText(ctx, "Error getting ext.", Toast.LENGTH_SHORT).show();
        }
        return name;
    }

    public static boolean checkUserValidity(Context ctx) {
        boolean res = false;
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            //check if the preference is there
            if (sharedPreferences.contains(RegistrationActivity.IsLogged)) {
                if (sharedPreferences.getString(RegistrationActivity.AccountType, "none").equalsIgnoreCase("user") && sharedPreferences.getBoolean(RegistrationActivity.IsLogged, false)) {
                    res = true;
                } else {
                    res = false;
                }
            } else {
                res = false;
            }
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), ctx);
            //return res;
        }
        return res;
    }

    public static boolean checkAccountIsLogged(Context ctx) {
        boolean res = false;
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            //check if the preference is there
            if (sharedPreferences.contains(RegistrationActivity.IsLogged)) {
                if (sharedPreferences.getBoolean(RegistrationActivity.IsLogged, false)) {
                    res = true;
                } else {
                    res = false;
                }
            } else {
                res = false;
            }
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), ctx);
            //return res;
        }
        return res;
    }

    public static String getUserAccountNo(Context ctx) {
        String res;
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            //check if the preference is there
            if (sharedPreferences.contains(RegistrationActivity.IsLogged)) {
                if (sharedPreferences.getString(RegistrationActivity.AccountType, "none").equalsIgnoreCase("user") && sharedPreferences.getBoolean(RegistrationActivity.IsLogged, false)) {
                    res = sharedPreferences.getString(RegistrationActivity.AccNo, "none");
                } else {
                    res = "admin";
                }
            } else {
                res = "unloged";
            }
        } catch (Exception e) {
            //methods.showAlert("Error",e.toString(),ctx);
            res = "error";
            return res;
        }
        return res;
    }

    public static String getUserProfile(Context ctx) {
        String res;
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            //check if the preference is there
            if (sharedPreferences.contains(RegistrationActivity.ProfileUrl)) {
                res = sharedPreferences.getString(RegistrationActivity.ProfileUrl, "none");
            } else {
                res = "none";
            }
        } catch (Exception e) {
            //methods.showAlert("Error",e.toString(),ctx);
            res = "error";
            return res;
        }
        return res;
    }

    public static int getUserId(Context ctx) {
        int res;
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            //check if the preference is there
            if (sharedPreferences.contains(RegistrationActivity.IsLogged)) {
                if (sharedPreferences.getBoolean(RegistrationActivity.IsLogged, false)) {
                    res = sharedPreferences.getInt(RegistrationActivity.UserId, 0);
                } else {
                    res = 0;
                }
            } else {
                res = 0;
            }
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), ctx);
            res = 0;
            return res;
        }
        return res;
    }

    public static boolean isAdmin(Context ctx) {
        boolean res = false;
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(RegistrationActivity.ACC_PREFERENCES, Context.MODE_PRIVATE);
            //check if the preference is there
            if (sharedPreferences.contains(RegistrationActivity.IsLogged)) {
                if (sharedPreferences.getString(RegistrationActivity.AccountType, "none").equalsIgnoreCase("admin") && sharedPreferences.getBoolean(RegistrationActivity.IsLogged, false)) {
                    res = true;
                } else {
                    res = false;
                }
            } else {
                res = false;
            }
        } catch (Exception e) {
            methods.showAlert("Error", e.toString(), ctx);
            //return res;
        }
        return res;
    }

    public static String getReadableDate(String date, Context ctx) {
        String finalDate = "";
        try {
            String[] tokens = date.split(" ");
            String[] tokens2 = tokens[0].split("/");
            String temp = "";
            switch (tokens2[0]) {
                case "1":
                    temp = "Jan";
                    break;
                case "2":
                    temp = "Feb";
                    break;
                case "3":
                    temp = "Mar";
                    break;
                case "4":
                    temp = "Apr";
                    break;
                case "5":
                    temp = "May";
                    break;
                case "6":
                    temp = "June";
                    break;
                case "7":
                    temp = "July";
                    break;
                case "8":
                    temp = "Aug";
                    break;
                case "9":
                    temp = "Sept";
                    break;
                case "10":
                    temp = "Oct";
                    break;
                case "11":
                    temp = "Nov";
                    break;
                case "12":
                    temp = "Dec";
                    break;
            }
            finalDate = tokens2[1] + " " + temp + " " + tokens2[2];
        } catch (Exception e) {
            Toast.makeText(ctx, "Error getting date.", Toast.LENGTH_SHORT).show();
        }
        return finalDate;
    }

    /*
     * performs a comparison between System current date and a date coming from DB (NB format has to be
     * MM/dd/yyyy - this date is generated from getDatev2 method(for current sys date)....
     * NB parameter passing System Current date must be first param and date from DB
     * second param
     */
    public static boolean compareCurrentDateAndDbDate(String current, String dbdate) {
        boolean res = false;
        try {
            String[] tokens1 = current.split("/");
            String[] tkns = dbdate.split(" ");
            String[] tokens2 = tkns[0].split("/");
            if (Integer.parseInt(tokens1[2]) > Integer.parseInt(tokens2[2])) {
                res = true;
            } else if (Integer.parseInt(tokens1[2]) == Integer.parseInt(tokens2[2])) {
                if (Integer.parseInt(tokens1[0]) > Integer.parseInt(tokens2[0])) {
                    res = true;
                } else if (Integer.parseInt(tokens1[0]) == Integer.parseInt(tokens2[0])) {
                    if (Integer.parseInt(tokens1[1]) > Integer.parseInt(tokens2[1])) {
                        res = true;
                    } else if (Integer.parseInt(tokens1[1]) == Integer.parseInt(tokens2[1])) {
                        res = false;
                    } else {
                        res = false;
                    }
                } else {
                    res = false;
                }
            } else {
                res = false;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    /*
     * compare date posted and end date for opportunity,if end date is less
     * tha date posted then deny
     */
    public static boolean checkOpportunityEndDate(String date_posted, String closing_date) {
        boolean res = false;
        try {
            String[] start = date_posted.split("-");
            String[] end = closing_date.split("-");
            //start
            String start_year = start[0];
            String start_mon = start[1];
            String start_day = start[2];
            //end
            String end_year = end[2];
            String end_mon = end[1];
            String end_day = end[0];

            if (Integer.parseInt(end_year) > Integer.parseInt(start_year)) {
                res = true;
            } else if (Integer.parseInt(end_year) == Integer.parseInt(start_year)) {
                if (Integer.parseInt(end_mon) > Integer.parseInt(start_mon)) {
                    res = true;
                } else if (Integer.parseInt(end_mon) == Integer.parseInt(start_mon)) {
                    if (Integer.parseInt(end_day) > Integer.parseInt(start_day)) {
                        res = true;
                    } else if (Integer.parseInt(end_day) == Integer.parseInt(start_day)) {
                        res = false;
                    } else {
                        res = false;
                    }
                } else {
                    res = false;
                }
            } else {
                res = false;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    /*
     * check for network
     */
    public static boolean isConnected(Context ctx) {
        boolean isConnected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            //if there is a network
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    isConnected = true;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    isConnected = true;
                }
            } else {
                isConnected = false;
            }
        } catch (Exception e) {
            showAlert("No network", "You have no internet connection", ctx);
        }
        return isConnected;
    }

    /*
     * default request options for glide
     */
    public static RequestOptions requestOptions(int resource) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.placeholder(resource);
        requestOptions.error(resource);
        return requestOptions;
    }
}
