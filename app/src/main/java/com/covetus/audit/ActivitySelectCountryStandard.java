package com.covetus.audit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ABS_CUSTOM_VIEW.TextViewBold;
import ABS_GET_SET.Audit;
import ABS_GET_SET.AuditMainLocation;
import ABS_GET_SET.AuditQuestion;
import ABS_GET_SET.AuditSubLocation;
import ABS_GET_SET.AuditSubQuestion;
import ABS_GET_SET.Inspector;
import ABS_HELPER.AppController;
import ABS_HELPER.CommonUtils;
import ABS_HELPER.DatabaseHelper;
import ABS_HELPER.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static ABS_HELPER.CommonUtils.hidePDialog;
import static ABS_HELPER.CommonUtils.mCheckSignalStrength;
import static ABS_HELPER.CommonUtils.mShowAlert;
import static Modal.AuditListModal.funUpdateAudit;
import static Modal.MainLocationModal.funInsertAllMainLocation;
import static Modal.SubLocationModal.funInsertAllSubLocation;
import static com.covetus.audit.ActivityTabHostMain.mStrCurrentTab;

public class ActivitySelectCountryStandard extends Activity {

    @BindView(R.id.mImageBack)
    ImageView mImageBack;
    @BindView(R.id.mLayoutDone)
    RelativeLayout mLayoutDone;
    @BindView(R.id.mSpinnerCountryStandard)
    Spinner mSpinnerCountryStandard;
    @BindView(R.id.mSpinnerLanguageStandard)
    Spinner mSpinnerLanguageStandard;
    HashMap<String, ArrayList<String>> meMap = new HashMap<String, ArrayList<String>>();
    HashMap<String, String> hmLanguage = new HashMap<String, String>();
    ArrayList<String> mListCountry = new ArrayList<>();
    ArrayList<String> mListCountryId = new ArrayList<>();
    String mStrCountryId, mStrLanguageCode;
    String mAuditId;
    DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country_language);
        ButterKnife.bind(this);

        db = new DatabaseHelper(ActivitySelectCountryStandard.this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mAuditId = bundle.getString("mAuditId");
        }


        if (mCheckSignalStrength(ActivitySelectCountryStandard.this) == 2) {
            CommonUtils.showPDialog(ActivitySelectCountryStandard.this);
            mToGetCountryStandard();
        } else {
            mShowAlert(getString(R.string.mTextFile_alert_no_internet), ActivitySelectCountryStandard.this);
        }


        mSpinnerCountryStandard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String mSelectedTitle = mListCountry.get(pos);
                mStrCountryId = mListCountryId.get(pos);
                if (mSelectedTitle.equals(getString(R.string.mTextFile_select_country_standard))) {
                    String[] mStringArray = new String[1];
                    mStringArray[0] = getString(R.string.mTextFile_select_lang);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ActivitySelectCountryStandard.this, R.layout.spinner_item, mStringArray);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                    mSpinnerLanguageStandard.setAdapter(spinnerArrayAdapter);

                } else {
                    ArrayList<String> listLanguage = meMap.get(mSelectedTitle);
                    listLanguage.add(0, getString(R.string.mTextFile_select_lang));
                    String[] mStringArray = new String[listLanguage.size()];
                    mStringArray = listLanguage.toArray(mStringArray);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ActivitySelectCountryStandard.this, R.layout.spinner_item, mStringArray);
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                    mSpinnerLanguageStandard.setAdapter(spinnerArrayAdapter);

                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });


        mSpinnerLanguageStandard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String mSelectedTitle = mSpinnerLanguageStandard.getSelectedItem().toString();
                if (mSelectedTitle.equals(getString(R.string.mTextFile_select_lang))) {
                    mStrLanguageCode = "0";
                } else {
                    mStrLanguageCode = hmLanguage.get(mSelectedTitle);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

    }

    /*click for going back*/
    @OnClick(R.id.mLayoutDone)
    public void mGoNext() {
        CommonUtils.OnClick(ActivitySelectCountryStandard.this, mLayoutDone);
        System.out.println("<><><>" + mStrCountryId);
        System.out.println("<><><>" + mStrLanguageCode);
        if (mStrCountryId.equals("null")) {
            CommonUtils.mShowAlert(getString(R.string.mTextFile_error_selcet_country_standard), ActivitySelectCountryStandard.this);
        }
        if (mStrCountryId.equals("0")) {
            CommonUtils.mShowAlert(getString(R.string.mTextFile_error_selcet_country_standard), ActivitySelectCountryStandard.this);
            return;
        } else if (mStrLanguageCode.equals("0")) {
            CommonUtils.mShowAlert(getString(R.string.mTextFile_error_selcet_language), ActivitySelectCountryStandard.this);
            return;
        }

        if (mCheckSignalStrength(ActivitySelectCountryStandard.this) == 2) {
            final Dialog dialog = new Dialog(ActivitySelectCountryStandard.this, R.style.Theme_Dialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_warning_popup_two_button);
            dialog.setCancelable(false);
            TextViewBold mTxtMsg = (TextViewBold) dialog.findViewById(R.id.mTxtMsg);
            RelativeLayout mLayoutYes = (RelativeLayout) dialog.findViewById(R.id.mLayoutYes);
            RelativeLayout mLayoutNo = (RelativeLayout) dialog.findViewById(R.id.mLayoutNo);
            mTxtMsg.setText(getResources().getString(R.string.mText_alert_audit_start));
            mLayoutNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            mLayoutYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.showPDialogMsg(ActivitySelectCountryStandard.this);
                    //mToGetQuestionAndInsertInDatabase();
                    //mToGetLocationStandard();
                    //mToGetSubLocationStandard();
                    db.delete_all_data_of_audit(mAuditId, PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                    mToGetLocationStandard();
                    dialog.cancel();
                }
            });
            dialog.show();
        } else {
            mShowAlert(getString(R.string.mTextFile_alert_no_internet), ActivitySelectCountryStandard.this);
        }

    }


    /*click for going back*/
    @OnClick(R.id.mImageBack)
    public void mGoBack() {
        finish();
    }


    /*api cal to get audit detail*/
    void mToGetCountryStandard() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, CommonUtils.mStrBaseUrl + "getCountryStandard",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String str) {
                        hidePDialog();
                        try {
                            System.out.println("<><><>" + str);
                            JSONObject response = new JSONObject(str);
                            String mStrStatus = response.getString("status");
                            if (mStrStatus.equals("1")) {
                                JSONObject jsonObject = response.getJSONObject("response");
                                JSONArray jsonArray = jsonObject.getJSONArray("standard");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    ArrayList<String> mListLanguage = new ArrayList<>();
                                    JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                                    String mStrCountry = jsonArrayObject.getString("country");
                                    String mStrCountryId = jsonArrayObject.getString("country_id");
                                    mListCountry.add(mStrCountry);
                                    mListCountryId.add(mStrCountryId);
                                    JSONArray arrayLanguage = jsonArrayObject.getJSONArray("language");
                                    for (int j = 0; j < arrayLanguage.length(); j++) {
                                        JSONObject jsonArrayObjLanguage = arrayLanguage.getJSONObject(j);
                                        String mStrLanguageId = jsonArrayObjLanguage.getString("id");
                                        String mStrLanguageTitle = jsonArrayObjLanguage.getString("title");
                                        String mStrLanguageLang = jsonArrayObjLanguage.getString("lang");
                                        mListLanguage.add(mStrLanguageTitle);
                                        hmLanguage.put(mStrLanguageTitle, mStrLanguageLang);
                                    }
                                    meMap.put(mStrCountry, mListLanguage);
                                }

                                mListCountry.add(0, getString(R.string.mTextFile_error_selcet_country));
                                mListCountryId.add(0, "0");
                                String[] mStringArray = new String[mListCountry.size()];
                                mStringArray = mListCountry.toArray(mStringArray);
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ActivitySelectCountryStandard.this, R.layout.spinner_item, mStringArray
                                );
                                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                                mSpinnerCountryStandard.setAdapter(spinnerArrayAdapter);
                            } else if (mStrStatus.equals("2")) {
                                CommonUtils.showSessionExp(ActivitySelectCountryStandard.this);
                            } else {
                                mShowAlert(response.getString("message"), ActivitySelectCountryStandard.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hidePDialog();
                        Toast.makeText(ActivitySelectCountryStandard.this, getString(R.string.mTextFile_error_something_went_wrong) + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                params.put("audit_id", mAuditId);
                params.put("auth_token", PreferenceManager.getFormiiAuthToken(ActivitySelectCountryStandard.this));
                System.out.println("<><><>#" + params);
                return params;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strRequest);
    }


    void mToGetLocationStandard() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, CommonUtils.mStrBaseUrl + "get-location",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String str) {

                        try {
                            System.out.println("<><><>" + str);
                            JSONObject response = new JSONObject(str);
                            String mStrStatus = response.getString("status");
                            if (mStrStatus.equals("1")) {
                                JSONArray jsonArray = response.getJSONArray("response");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObjectArr = jsonArray.getJSONObject(i);
                                    String mStrId = jsonObjectArr.getString("id");
                                    String mStrTitle = jsonObjectArr.getString("title");
                                    String mStrDetail = jsonObjectArr.getString("details");
                                    AuditMainLocation auditMainLocation = new AuditMainLocation();
                                    auditMainLocation.setmStrAuditId(mAuditId);//audit id
                                    auditMainLocation.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));//user id
                                    auditMainLocation.setmStrLocationServerId(mStrId);
                                    auditMainLocation.setmStrLocationTitle(mStrTitle);
                                    auditMainLocation.setmStrLocationDesc(mStrDetail);
                                    funInsertAllMainLocation(auditMainLocation, db);
                            /*if(i==jsonArray.length()-1){
                            getLocationContent();
                            }*/
                                }
                                mToGetSubLocationStandard();
                            } else if (mStrStatus.equals("2")) {
                                CommonUtils.showSessionExp(ActivitySelectCountryStandard.this);
                            } else {
                                mShowAlert(response.getString("message"), ActivitySelectCountryStandard.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hidePDialog();
                        Toast.makeText(ActivitySelectCountryStandard.this, getString(R.string.mTextFile_error_something_went_wrong) + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                params.put("auth_token", PreferenceManager.getFormiiAuthToken(ActivitySelectCountryStandard.this));
                params.put("audit_id", mAuditId);
                params.put("lang", mStrLanguageCode);
                params.put("country_id", mStrCountryId);
                return params;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    void mToGetSubLocationStandard() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, CommonUtils.mStrBaseUrl + "getContent",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String str) {

                        try {
                            System.out.println("<><><>" + str);
                            JSONObject response = new JSONObject(str);
                            String mStrStatus = response.getString("status");
                            if (mStrStatus.equals("1")) {
                                JSONArray jsonArray = response.getJSONArray("response");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObjectArr = jsonArray.getJSONObject(i);
                                    String mStrId = jsonObjectArr.getString("id");
                                    String mStrTitle = jsonObjectArr.getString("title");
                                    String mStrDetail = jsonObjectArr.getString("details");
                                    String mStrMainLocation = jsonObjectArr.getString("location_id");
                                    AuditSubLocation auditSubLocation = new AuditSubLocation();
                                    auditSubLocation.setmStrAuditId(mAuditId);
                                    auditSubLocation.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                    auditSubLocation.setmStrMainLocationServer(mStrMainLocation);
                                    auditSubLocation.setmStrMainLocationLocal("demo");
                                    auditSubLocation.setmStrSubLocationServerId(mStrId);
                                    auditSubLocation.setmStrSubLocationTitle(mStrTitle);
                                    auditSubLocation.setmStrSubLocationDesc(mStrDetail);
                                    funInsertAllSubLocation(auditSubLocation, db);
                            /*if(i==jsonArray.length()-1){
                            getLocationContent();
                            }*/
                                }
                                mToGetQuestion();
                            } else if (mStrStatus.equals("2")) {
                                CommonUtils.showSessionExp(ActivitySelectCountryStandard.this);
                            } else {
                                mShowAlert(response.getString("message"), ActivitySelectCountryStandard.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hidePDialog();
                        Toast.makeText(ActivitySelectCountryStandard.this, getString(R.string.mTextFile_error_something_went_wrong) + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                params.put("auth_token", PreferenceManager.getFormiiAuthToken(ActivitySelectCountryStandard.this));
                params.put("audit_id", mAuditId);
                params.put("lang", mStrLanguageCode);
                params.put("country_id", mStrCountryId);
                return params;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    void RecurJson(JSONArray jsonArray,String mStrMainLocation,String mStrSubLocation, String mStrNormalQuestionID ) {
        try {
            for (int b = 0; b < jsonArray.length(); b++) {
                JSONObject jsonObjectSub = jsonArray.getJSONObject(b);
                String mStrSUBQuestionID = jsonObjectSub.getString("id");
                String mStrSUBQuestion = jsonObjectSub.getString("question");
                String mStrSUBQuesAnswer = jsonObjectSub.getString("answer");
                String mStrSUBAnswerID = jsonObjectSub.getString("answernum");
                String mStrSUBAnswerType = jsonObjectSub.getString("type");
                String mStrSUBLocation = jsonObjectSub.getString("content_id");
                String mStrQuestionCondition = jsonObjectSub.getString("answer_id");
                JSONArray jsonArraySUB = jsonObjectSub.getJSONArray("sub_questions");
                System.out.println("<>###<><>###<><>## "+mStrSUBQuestionID);

                AuditSubQuestion auditSubQuestion = new AuditSubQuestion();
                auditSubQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                auditSubQuestion.setmStrAuditId(mAuditId);
                auditSubQuestion.setmStrServerId(mStrSUBQuestionID);
                auditSubQuestion.setmStrQuestionTxt(mStrSUBQuestion.replace("*", "#"));
                if (mStrSUBQuesAnswer.contains("\\r\\n")) {
                    mStrSUBQuesAnswer = mStrSUBQuesAnswer.replace("\\r\\n", "\r\n");
                }
                auditSubQuestion.setmStrAnswerOption(mStrSUBQuesAnswer.replace("\r\n", "#"));
                auditSubQuestion.setmStrAnswerOptionId(mStrSUBAnswerID.replace(",", "#"));
                auditSubQuestion.setmStrQuestionType(mStrSUBAnswerType);
                auditSubQuestion.setmStrMainLocation(mStrMainLocation);
                auditSubQuestion.setmStrMainQuestion(mStrNormalQuestionID + "");
                auditSubQuestion.setmStrQuestionFor(mStrQuestionCondition);
                auditSubQuestion.setmStrSubLocation(mStrSubLocation);
                if (jsonArraySUB.length() > 0) {
                    auditSubQuestion.setmStrSubQuestion("1");
                } else {
                    auditSubQuestion.setmStrSubQuestion("0");
                }

                if (mStrSUBAnswerType.equals("5")) {

                } else {
                    db.insert_tb_audit_sub_questions(auditSubQuestion);
                }
                if(jsonArraySUB.length()>0){
                RecurJson(jsonArraySUB,mStrMainLocation,mStrSubLocation,mStrSUBQuestionID);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    void mToGetQuestion() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, CommonUtils.mStrBaseUrl + "getFinalQuestion",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String str) {
                        hidePDialog();
                        try {

                            JSONObject response = new JSONObject(str);
                            System.out.println("<><><>" + response.toString());
                            String mStrStatus = response.getString("status");
                            if (mStrStatus.equals("1")) {
                                JSONObject jsonObject = response.getJSONObject("response");
                                String mStrMainLocation = "demo";




                                JSONArray JANormalQuestion = jsonObject.getJSONArray("normal_questions");
                                for (int l = 0; l < JANormalQuestion.length(); l++) {
                                    JSONArray jsonArray = JANormalQuestion.getJSONArray(l);
                                    for (int u = 0; u < jsonArray.length(); u++) {
                                        JSONObject JOMeasurementQuestion = jsonArray.getJSONObject(u);
                                        String mStrNormalQuestionID = JOMeasurementQuestion.getString("id");
                                        String mStrNormalQuestion = JOMeasurementQuestion.getString("question");
                                        String mStrNormalQuesAnswer = JOMeasurementQuestion.getString("answer");
                                        String mStrNormalAnswerID = JOMeasurementQuestion.getString("answernum");
                                        String mStrAnswerType = JOMeasurementQuestion.getString("type");
                                        String mStrSubLocation = JOMeasurementQuestion.getString("content_id");
                                        JSONArray JASubQuestion = JOMeasurementQuestion.getJSONArray("sub_questions");



                                        JSONArray JAInspectorQuestion = JOMeasurementQuestion.getJSONArray("ins_questions");
                                        AuditQuestion auditQuestion = new AuditQuestion();
                                        auditQuestion.setmStrAuditId(mAuditId);
                                        auditQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                        auditQuestion.setmStrMainLocation(mStrMainLocation + "");
                                        auditQuestion.setmStrSubLocation(mStrSubLocation + "");
                                        System.out.println("<>##<> " + mStrNormalQuestion);
                                        auditQuestion.setmStrQuestionTxt(mStrNormalQuestion.replace("*", "#"));
                                        auditQuestion.setmStrServerId(mStrNormalQuestionID);

                                        if (mStrNormalQuesAnswer.contains("\\r\\n")) {
                                            mStrNormalQuesAnswer = mStrNormalQuesAnswer.replace("\\r\\n", "\r\n");
                                        }
                                        auditQuestion.setmStrAnswerOption(mStrNormalQuesAnswer.replace("\r\n", "#"));
                                        auditQuestion.setmStrAnswerOptionId(mStrNormalAnswerID.replace(",", "#"));
                                        auditQuestion.setmStrQuestionType(mStrAnswerType);
                                        auditQuestion.setmStrQuestionStatus("1");

                                        if (JASubQuestion.length() > 0) {
                                            auditQuestion.setmStrSubQuestion("1");
                                        } else {
                                            auditQuestion.setmStrSubQuestion("0");
                                        }

                                        if (JAInspectorQuestion.length() > 0) {
                                            auditQuestion.setmStrInspectorQuestion("1");
                                        } else {
                                            auditQuestion.setmStrInspectorQuestion("0");

                                        }


                                        if (mStrAnswerType.equals("5")) {

                                        } else {
                                            db.insert_tb_audit_question(auditQuestion);
                                        }

                                        if(JAInspectorQuestion.length()>0){
                                            for (int p = 0; p < JAInspectorQuestion.length(); p++) {
                                                JSONObject JOInspectorQuestion = JAInspectorQuestion.getJSONObject(p);
                                                String mStrInspectorQuestionId = JOInspectorQuestion.getString("id");
                                                String mStrInspectorQuestion = JOInspectorQuestion.getString("question");
                                                String mStrInspectorQuestionAns = JOInspectorQuestion.getString("answer");
                                                String mStrInspectorQuestionAnsID = JOInspectorQuestion.getString("answernum");
                                                String mStrInspectorAnsType = JOInspectorQuestion.getString("type");


                                                if (mStrInspectorQuestion.contains("\\r\\n")) {
                                                    mStrInspectorQuestion = mStrInspectorQuestion.replace("\\r\\n", "\r\n");
                                                }

                                                Inspector inspector = new Inspector();
                                                inspector.setmStrAuditId(mAuditId);
                                                inspector.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                inspector.setmStrMainLocation(mStrMainLocation);
                                                inspector.setmStrSubLocation(mStrSubLocation);
                                                inspector.setmStrMainQuestion(mStrNormalQuestionID);
                                                inspector.setmStrQuestionId(mStrInspectorQuestionId);
                                                inspector.setmStrQuestionTxt(mStrInspectorQuestion.replace("*", "#"));
                                                inspector.setmStrQuestionType(mStrInspectorAnsType);
                                                inspector.setmStrAnswerOption(mStrInspectorQuestionAns.replace("\r\n", "#"));
                                                inspector.setmStrAnswerOptionId(mStrInspectorQuestionAnsID.replace(",", "#"));
                                                db.insert_tb_inspector_questions(inspector);
                                            }

                                        }


                                        if(JASubQuestion.length()>0){
                                            RecurJson(JASubQuestion,mStrMainLocation,mStrSubLocation,mStrNormalQuestionID);
                                        }


                                       /* for (int m = 0; m < JASubQuestion.length(); m++) {
                                            JSONObject JOSubQuestion = JASubQuestion.getJSONObject(m);
                                            String mStrSubQuestionId = JOSubQuestion.getString("id");
                                            String mStrSubQuestion = JOSubQuestion.getString("question");
                                            String mStrSubQuestionAns = JOSubQuestion.getString("answer");
                                            String mStrSubQuestionAnsID = JOSubQuestion.getString("answernum");
                                            String mStrQuestionCondition = JOSubQuestion.getString("answer_id");
                                            String mStrSubAnsType = JOSubQuestion.getString("type");
                                            JSONArray mArraySubQuestion = JOSubQuestion.getJSONArray("sub_questions");

                                            AuditSubQuestion auditSubQuestion = new AuditSubQuestion();
                                            auditSubQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                            auditSubQuestion.setmStrAuditId(mAuditId);
                                            auditSubQuestion.setmStrServerId(mStrSubQuestionId);
                                            auditSubQuestion.setmStrQuestionTxt(mStrSubQuestion.replace("*", "#"));
                                            if (mStrSubQuestionAns.contains("\\r\\n")) {
                                                mStrSubQuestionAns = mStrSubQuestionAns.replace("\\r\\n", "\r\n");
                                            }
                                            auditSubQuestion.setmStrAnswerOption(mStrSubQuestionAns.replace("\r\n", "#"));
                                            auditSubQuestion.setmStrAnswerOptionId(mStrSubQuestionAnsID.replace(",", "#"));
                                            auditSubQuestion.setmStrQuestionType(mStrSubAnsType);
                                            auditSubQuestion.setmStrMainLocation(mStrMainLocation);
                                            auditSubQuestion.setmStrMainQuestion(mStrNormalQuestionID + "");
                                            auditSubQuestion.setmStrQuestionFor(mStrQuestionCondition);
                                            auditSubQuestion.setmStrSubLocation(mStrSubLocation);
                                            if (mArraySubQuestion.length() > 0) {
                                                auditSubQuestion.setmStrSubQuestion("1");
                                            } else {
                                                auditSubQuestion.setmStrSubQuestion("0");
                                            }

                                            if (mStrSubAnsType.equals("5")) {

                                            } else {
                                                db.insert_tb_audit_sub_questions(auditSubQuestion);
                                            }

                                            if (mArraySubQuestion.length() > 0) {
                                                for (int z = 0; z < mArraySubQuestion.length(); z++) {
                                                    JSONObject SubQuestion = mArraySubQuestion.getJSONObject(z);

                                                    String mStrSubQuestionId2 = SubQuestion.getString("id");
                                                    String mStrSubQuestion2 = SubQuestion.getString("question");
                                                    String mStrSubQuestionAns2 = SubQuestion.getString("answer");
                                                    String mStrSubQuestionAnsID2 = SubQuestion.getString("answernum");
                                                    String mStrQuestionCondition2 = SubQuestion.getString("answer_id");
                                                    String mStrSubAnsType2 = SubQuestion.getString("type");
                                                    JSONArray mArraySubQuestion2 = SubQuestion.getJSONArray("sub_questions");
                                                    AuditSubQuestion auditSubQuestion2 = new AuditSubQuestion();
                                                    auditSubQuestion2.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                    auditSubQuestion2.setmStrAuditId(mAuditId);
                                                    auditSubQuestion2.setmStrServerId(mStrSubQuestionId2);
                                                    auditSubQuestion2.setmStrQuestionTxt(mStrSubQuestion2.replace("*", "#"));
                                                    if (mStrSubQuestionAns2.contains("\\r\\n")) {
                                                        auditSubQuestion2.setmStrAnswerOption(mStrSubQuestionAns2.replace("\\r\\n", "\r\n"));
                                                    }
                                                    auditSubQuestion2.setmStrAnswerOption(mStrSubQuestionAns2.replace("\r\n", "#"));
                                                    auditSubQuestion2.setmStrAnswerOptionId(mStrSubQuestionAnsID2.replace(",", "#"));
                                                    auditSubQuestion2.setmStrQuestionType(mStrSubAnsType2);
                                                    auditSubQuestion2.setmStrMainQuestion(mStrSubQuestionId + "");
                                                    auditSubQuestion2.setmStrQuestionFor(mStrQuestionCondition2);
                                                    auditSubQuestion2.setmStrMainLocation(mStrMainLocation);
                                                    auditSubQuestion2.setmStrSubLocation(mStrSubLocation);
                                                    if (mArraySubQuestion2.length() > 0) {
                                                        auditSubQuestion2.setmStrSubQuestion("1");
                                                    } else {
                                                        auditSubQuestion2.setmStrSubQuestion("0");
                                                    }


                                                    if (mStrSubAnsType2.equals("5")) {

                                                    } else {
                                                        db.insert_tb_audit_sub_questions(auditSubQuestion2);
                                                    }


                                                }
                                            }


                                        }*/


                                    }


                                }


                                JSONArray JAMeasurementQuestion = jsonObject.getJSONArray("measurement_questions");

                                for (int l = 0; l < JAMeasurementQuestion.length(); l++) {
                                    JSONArray jsonArray = JAMeasurementQuestion.getJSONArray(l);
                                    for (int p = 0; p < jsonArray.length(); p++) {
                                        JSONObject JOMeasurementQuestion = jsonArray.getJSONObject(p);
                                        String mStrNormalQuestionID = JOMeasurementQuestion.getString("id");
                                        String mStrNormalQuestion = JOMeasurementQuestion.getString("question");
                                        String mStrNormalQuesAnswer = JOMeasurementQuestion.getString("answer");
                                        String mStrNormalAnswerID = JOMeasurementQuestion.getString("answernum");
                                        String mStrAnswerType = JOMeasurementQuestion.getString("type");
                                        String mStrSubLocation = JOMeasurementQuestion.getString("content_id");
                                        JSONArray JASubQuestion = JOMeasurementQuestion.getJSONArray("sub_questions");

                                        JSONArray JAInspectorQuestion = JOMeasurementQuestion.getJSONArray("ins_questions");

                                        System.out.println("<><><><>JASubQuestion" + JASubQuestion.toString());
                                        AuditQuestion auditQuestion = new AuditQuestion();
                                        auditQuestion.setmStrAuditId(mAuditId);
                                        auditQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                        auditQuestion.setmStrMainLocation(mStrMainLocation + "");
                                        auditQuestion.setmStrSubLocation(mStrSubLocation + "");
                                        auditQuestion.setmStrQuestionTxt(mStrNormalQuestion.replace("*", "#"));
                                        auditQuestion.setmStrServerId(mStrNormalQuestionID);
                                        auditQuestion.setmStrAnswerOption(mStrNormalQuesAnswer.replace("\r\n", "#"));
                                        auditQuestion.setmStrAnswerOptionId(mStrNormalAnswerID.replace(",", "#"));
                                        auditQuestion.setmStrQuestionType(mStrAnswerType);
                                        auditQuestion.setmStrQuestionStatus("0");
                                        if (JASubQuestion.length() > 0) {
                                            auditQuestion.setmStrSubQuestion("1");
                                        } else {
                                            auditQuestion.setmStrSubQuestion("0");
                                        }

                                        if (JAInspectorQuestion.length() > 0) {
                                            auditQuestion.setmStrInspectorQuestion("1");
                                        } else {
                                            auditQuestion.setmStrInspectorQuestion("0");

                                        }
                                        if (mStrAnswerType.equals("5")) {

                                        } else {
                                            db.insert_tb_audit_question(auditQuestion);
                                        }

                                        if(JASubQuestion.length()>0){
                                            RecurJson(JASubQuestion,mStrMainLocation,mStrSubLocation,mStrNormalQuestionID);
                                        }


                                      /*  for (int m = 0; m < JASubQuestion.length(); m++) {
                                            JSONObject JOSubQuestion = JASubQuestion.getJSONObject(m);
                                            String mStrSubQuestionId = JOSubQuestion.getString("id");
                                            String mStrSubQuestion = JOSubQuestion.getString("question");
                                            String mStrSubQuestionAns = JOSubQuestion.getString("answer");
                                            String mStrSubQuestionAnsID = JOSubQuestion.getString("answernum");
                                            String mStrQuestionCondition = JOSubQuestion.getString("answer_id");
                                            String mStrSubAnsType = JOSubQuestion.getString("type");
                                            JSONArray mArraySubQuestion = JOSubQuestion.getJSONArray("sub_questions");
                                            AuditSubQuestion auditSubQuestion = new AuditSubQuestion();
                                            auditSubQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                            auditSubQuestion.setmStrAuditId(mAuditId);
                                            auditSubQuestion.setmStrServerId(mStrSubQuestionId);
                                            auditSubQuestion.setmStrQuestionTxt(mStrSubQuestion.replace("*", "#"));
                                            auditSubQuestion.setmStrAnswerOption(mStrSubQuestionAns.replace("\r\n", "#"));
                                            auditSubQuestion.setmStrAnswerOptionId(mStrSubQuestionAnsID.replace(",", "#"));
                                            auditSubQuestion.setmStrQuestionType(mStrSubAnsType);
                                            auditSubQuestion.setmStrMainLocation(mStrMainLocation);
                                            auditSubQuestion.setmStrMainQuestion(mStrNormalQuestionID + "");
                                            auditSubQuestion.setmStrQuestionFor(mStrQuestionCondition);
                                            auditSubQuestion.setmStrSubLocation(mStrSubLocation);
                                            if (mArraySubQuestion.length() > 0) {
                                                auditSubQuestion.setmStrSubQuestion("1");
                                            } else {
                                                auditSubQuestion.setmStrSubQuestion("0");
                                            }


                                            if (mStrSubAnsType.equals("5")) {

                                            } else {
                                                db.insert_tb_audit_sub_questions(auditSubQuestion);
                                            }

                                            if (mArraySubQuestion.length() > 0) {
                                                for (int z = 0; z < mArraySubQuestion.length(); z++) {
                                                    JSONObject SubQuestion = mArraySubQuestion.getJSONObject(z);
                                                    String mStrSubQuestionId2 = SubQuestion.getString("id");
                                                    String mStrSubQuestion2 = SubQuestion.getString("question");
                                                    String mStrSubQuestionAns2 = SubQuestion.getString("answer");
                                                    String mStrSubQuestionAnsID2 = SubQuestion.getString("answernum");
                                                    String mStrQuestionCondition2 = SubQuestion.getString("answer_id");
                                                    String mStrSubAnsType2 = SubQuestion.getString("type");
                                                    JSONArray mArraySubQuestion2 = SubQuestion.getJSONArray("sub_questions");
                                                    AuditSubQuestion auditSubQuestion2 = new AuditSubQuestion();
                                                    auditSubQuestion2.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                    auditSubQuestion2.setmStrAuditId(mAuditId);
                                                    auditSubQuestion2.setmStrServerId(mStrSubQuestionId2);
                                                    auditSubQuestion2.setmStrQuestionTxt(mStrSubQuestion2.replace("*", "#"));
                                                    auditSubQuestion2.setmStrAnswerOption(mStrSubQuestionAns2.replace("\r\n", "#"));
                                                    auditSubQuestion2.setmStrAnswerOptionId(mStrSubQuestionAnsID2.replace(",", "#"));
                                                    auditSubQuestion2.setmStrQuestionType(mStrSubAnsType2);
                                                    auditSubQuestion2.setmStrMainQuestion(mStrSubQuestionId + "");
                                                    auditSubQuestion2.setmStrMainLocation(mStrMainLocation);
                                                    auditSubQuestion2.setmStrSubLocation(mStrSubLocation);

                                                    auditSubQuestion2.setmStrQuestionFor(mStrQuestionCondition2);
                                                    if (mArraySubQuestion2.length() > 0) {
                                                        auditSubQuestion2.setmStrSubQuestion("1");
                                                    } else {
                                                        auditSubQuestion2.setmStrSubQuestion("0");
                                                    }

                                                    if (mStrSubAnsType2.equals("5")) {

                                                    } else {
                                                        db.insert_tb_audit_sub_questions(auditSubQuestion2);
                                                    }


                                                }
                                            }


                                        }*/

                                    }
                                }


                                Audit audit = new Audit();
                                audit.setmAuditId(mAuditId);
                                audit.setmStrCountryId(mStrCountryId);
                                audit.setmStrLangId(mStrLanguageCode);
                                funUpdateAudit(audit, "2", db);
                                //hidePDialog();
                                mStrCurrentTab = "0";
                                Intent intent = new Intent(ActivitySelectCountryStandard.this, SelectMainLocationActivity.class);
                                intent.putExtra("mAuditId", mAuditId);
                                startActivity(intent);
                                finish();
                            } else if (mStrStatus.equals("2")) {
                                System.out.println("<><>##<><> call1");
                                CommonUtils.showSessionExp(ActivitySelectCountryStandard.this);
                            } else {
                                System.out.println("<><>##<><> call2");
                                mShowAlert(response.getString("message"), ActivitySelectCountryStandard.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hidePDialog();
                        Toast.makeText(ActivitySelectCountryStandard.this, getString(R.string.mTextFile_error_something_went_wrong) + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                params.put("auth_token", PreferenceManager.getFormiiAuthToken(ActivitySelectCountryStandard.this));
                params.put("audit_id", mAuditId);
                params.put("lang", mStrLanguageCode);
                params.put("country_id", mStrCountryId);
                System.out.println("<><>** " + params);
                return params;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strRequest);
    }


  /*  void getLocationContent(){
    ArrayList<AuditMainLocation> mAuditList = new ArrayList<AuditMainLocation>();
    mAuditList = funGetAllMainLocation(mAuditId,db);
    System.out.println("<><>#### 123call " + mAuditList.size());
    for (int i = 0; i<mAuditList.size();i++){
    mToGetQuestionAndInsertInDatabase(mAuditList.get(i).getmStrLocationServerId(),mAuditList.get(i).getmStrId());
    if(i==mAuditList.size()-1){

        hidePDialog();
        Audit audit = new Audit();
        audit.setmAuditId(mAuditId);
        audit.setmStrCountryId(mStrCountryId);
        audit.setmStrLangId(mStrLanguageCode);
        funUpdateAudit(audit,"2",db);
        //hidePDialog();
        mStrCurrentTab = "0";
        Intent intent = new Intent(ActivitySelectCountryStandard.this, SelectMainLocationActivity.class);
        intent.putExtra("mAuditId", mAuditId);
        startActivity(intent);
        finish();
    }
    }
   }*/


    /*api cal to get audit detail*/
    /*void mToGetQuestionAndInsertInDatabase(final String mStrLocationId,final String mLocationLocal) {
        StringRequest strRequest = new StringRequest(Request.Method.POST, CommonUtils.mStrBaseUrl + "getQuestionByLocation",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String str) {
                        System.out.println("<><>##" + str);
                        try {
                            System.out.println("<><><>" + str);
                            JSONObject response = new JSONObject(str);
                            String mStrStatus = response.getString("status");
                            if (mStrStatus.equals("1")) {
                                JSONArray jsonArray = response.getJSONArray("response");
                                if (jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                                    JSONArray JAMiltiLangModules = jsonArrayObject.getJSONArray("Milti_lang_modules");
                                    for (int j = 0; j < JAMiltiLangModules.length(); j++) {
                                        JSONObject JOMainCategory = JAMiltiLangModules.getJSONObject(j);
                                        // main category
                                       *//**//**//**//**//**//**//**//* String mStrMainCatID = JOMainCategory.getString("id");
                                        String mStrMainCatTitle = JOMainCategory.getString("title");
                                        String mStrMainCatDesc = JOMainCategory.getString("desc");*//**//**//**//**//**//**//**//*

                                       AuditMainLocation auditMainLocation = new AuditMainLocation();
                                        auditMainLocation.setmStrAuditId(mAuditId);//audit id
                                        auditMainLocation.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));//user id
                                        auditMainLocation.setmStrLocationServerId(mStrMainCatID);
                                        auditMainLocation.setmStrLocationTitle(mStrMainCatTitle);
                                        auditMainLocation.setmStrLocationDesc(mStrMainCatDesc);
                                        funInsertAllMainLocation(auditMainLocation,db);
                                        JSONArray JASubCat = JOMainCategory.getJSONArray("categories");
                                        for (int k = 0; k < JASubCat.length(); k++) {
                                            JSONObject JOSubCategory = JASubCat.getJSONObject(k);
                                            //sub category
                                            String mStrSubCatID = JOSubCategory.getString("id");
                                            String mStrSubCatTitle = JOSubCategory.getString("title");
                                            String mStrSubCatDesc = JOSubCategory.getString("desc");
                                            AuditSubLocation auditSubLocation = new AuditSubLocation();
                                            auditSubLocation.setmStrAuditId(mAuditId);
                                            auditSubLocation.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                            auditSubLocation.setmStrMainLocationServer(mStrLocationId);
                                            auditSubLocation.setmStrMainLocationLocal("demo");
                                            auditSubLocation.setmStrSubLocationServerId(mStrSubCatID);
                                            auditSubLocation.setmStrSubLocationTitle(mStrSubCatTitle);
                                            auditSubLocation.setmStrSubLocationDesc(mStrSubCatDesc);
                                            int subLocationLocalID = funInsertAllSubLocation(auditSubLocation, db);
                                            //int subLocationLocalID = db.insert_tb_audit_sub_location(auditSubLocation);
                                            JSONArray JANormalQuestion = JOSubCategory.getJSONArray("normal_questions");
                                            for (int l = 0; l < JANormalQuestion.length(); l++) {
                                                JSONObject JOMeasurementQuestion = JANormalQuestion.getJSONObject(l);
                                                String mStrNormalQuestionID = JOMeasurementQuestion.getString("id");
                                                String mStrNormalQuestion = JOMeasurementQuestion.getString("question");
                                                String mStrNormalQuesAnswer = JOMeasurementQuestion.getString("answer");
                                                String mStrNormalAnswerID = JOMeasurementQuestion.getString("answernum");
                                                String mStrAnswerType = JOMeasurementQuestion.getString("type");
                                                JSONArray JASubQuestion = JOMeasurementQuestion.getJSONArray("sub_questions");

                                                JSONArray JAInspectorQuestion = JOMeasurementQuestion.getJSONArray("ins_questions");


                                                AuditQuestion auditQuestion = new AuditQuestion();
                                                auditQuestion.setmStrAuditId(mAuditId);
                                                auditQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                auditQuestion.setmStrMainLocation(mStrLocationId + "");
                                                auditQuestion.setmStrSubLocation(mStrSubCatID + "");
                                                System.out.println("<>##<> "+mStrNormalQuestion);
                                                auditQuestion.setmStrQuestionTxt(mStrNormalQuestion);
                                                auditQuestion.setmStrServerId(mStrNormalQuestionID);

                                                if (mStrNormalQuesAnswer.contains("\\r\\n")) {
                                                    mStrNormalQuesAnswer = mStrNormalQuesAnswer.replace("\\r\\n", "\r\n");
                                                }
                                                auditQuestion.setmStrAnswerOption(mStrNormalQuesAnswer.replace("\r\n", "#"));
                                                auditQuestion.setmStrAnswerOptionId(mStrNormalAnswerID.replace(",", "#"));
                                                auditQuestion.setmStrQuestionType(mStrAnswerType);
                                                auditQuestion.setmStrQuestionStatus("1");

                                                if (JASubQuestion.length() > 0) {
                                                    auditQuestion.setmStrSubQuestion("1");
                                                } else {
                                                    auditQuestion.setmStrSubQuestion("0");
                                                }

                                                if (JAInspectorQuestion.length() > 0) {
                                                    auditQuestion.setmStrInspectorQuestion("1");
                                                } else {
                                                    auditQuestion.setmStrInspectorQuestion("0");

                                                }


                                                if (mStrAnswerType.equals("5")) {

                                                } else {
                                                db.insert_tb_audit_question(auditQuestion);
                                                }
                                                for (int p = 0; p < JAInspectorQuestion.length(); p++) {
                                                    JSONObject JOInspectorQuestion = JAInspectorQuestion.getJSONObject(p);
                                                    String mStrInspectorQuestionId = JOInspectorQuestion.getString("id");
                                                    String mStrInspectorQuestion = JOInspectorQuestion.getString("question");
                                                    String mStrInspectorQuestionAns = JOInspectorQuestion.getString("answer");
                                                    String mStrInspectorQuestionAnsID = JOInspectorQuestion.getString("answernum");
                                                    String mStrInspectorAnsType = JOInspectorQuestion.getString("type");


                                                    if (mStrInspectorQuestion.contains("\\r\\n")) {
                                                        mStrInspectorQuestion = mStrInspectorQuestion.replace("\\r\\n", "\r\n");
                                                    }

                                                    Inspector inspector = new Inspector();
                                                    inspector.setmStrAuditId(mAuditId);
                                                    inspector.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                    inspector.setmStrMainLocation(mStrLocationId);
                                                    inspector.setmStrSubLocation(mStrSubCatID);
                                                    inspector.setmStrMainQuestion(mStrNormalQuestionID);
                                                    inspector.setmStrQuestionId(mStrInspectorQuestionId);
                                                    inspector.setmStrQuestionTxt(mStrInspectorQuestion);
                                                    inspector.setmStrQuestionType(mStrInspectorAnsType);
                                                    inspector.setmStrAnswerOption(mStrInspectorQuestionAns.replace("\r\n", "#"));
                                                    inspector.setmStrAnswerOptionId(mStrInspectorQuestionAnsID.replace(",", "#"));
                                                    db.insert_tb_inspector_questions(inspector);
                                                }
                                                for (int m = 0; m < JASubQuestion.length(); m++) {
                                                    JSONObject JOSubQuestion = JASubQuestion.getJSONObject(m);
                                                    String mStrSubQuestionId = JOSubQuestion.getString("id");
                                                    String mStrSubQuestion = JOSubQuestion.getString("question");
                                                    String mStrSubQuestionAns = JOSubQuestion.getString("answer");
                                                    String mStrSubQuestionAnsID = JOSubQuestion.getString("answernum");
                                                    String mStrQuestionCondition = JOSubQuestion.getString("answer_id");
                                                    String mStrSubAnsType = JOSubQuestion.getString("type");
                                                    JSONArray mArraySubQuestion = JOSubQuestion.getJSONArray("sub_questions");
                                                    AuditSubQuestion auditSubQuestion = new AuditSubQuestion();
                                                    auditSubQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                    auditSubQuestion.setmStrAuditId(mAuditId);
                                                    auditSubQuestion.setmStrServerId(mStrSubQuestionId);
                                                    auditSubQuestion.setmStrQuestionTxt(mStrSubQuestion);
                                                    if (mStrSubQuestionAns.contains("\\r\\n")) {
                                                        mStrSubQuestionAns = mStrSubQuestionAns.replace("\\r\\n", "\r\n");
                                                    }
                                                    auditSubQuestion.setmStrAnswerOption(mStrSubQuestionAns.replace("\r\n", "#"));
                                                    auditSubQuestion.setmStrAnswerOptionId(mStrSubQuestionAnsID.replace(",", "#"));
                                                    auditSubQuestion.setmStrQuestionType(mStrSubAnsType);
                                                    auditSubQuestion.setmStrMainLocation(mStrLocationId);
                                                    auditSubQuestion.setmStrMainQuestion(mStrNormalQuestionID + "");
                                                    auditSubQuestion.setmStrQuestionFor(mStrQuestionCondition);
                                                    auditSubQuestion.setmStrSubLocation(mStrSubCatID);
                                                    if (mArraySubQuestion.length() > 0) {
                                                        auditSubQuestion.setmStrSubQuestion("1");
                                                    } else {
                                                        auditSubQuestion.setmStrSubQuestion("0");
                                                    }

                                                    if (mStrSubAnsType.equals("5")) {

                                                    } else {
                                                       db.insert_tb_audit_sub_questions(auditSubQuestion);
                                                    }

                                                    if (mArraySubQuestion.length() > 0) {
                                                        for (int z = 0; z < mArraySubQuestion.length(); z++) {
                                                            JSONObject SubQuestion = mArraySubQuestion.getJSONObject(z);

                                                            String mStrSubQuestionId2 = SubQuestion.getString("id");
                                                            String mStrSubQuestion2 = SubQuestion.getString("question");
                                                            String mStrSubQuestionAns2 = SubQuestion.getString("answer");
                                                            String mStrSubQuestionAnsID2 = SubQuestion.getString("answernum");
                                                            String mStrQuestionCondition2 = SubQuestion.getString("answer_id");
                                                            String mStrSubAnsType2 = SubQuestion.getString("type");
                                                            JSONArray mArraySubQuestion2 = SubQuestion.getJSONArray("sub_questions");
                                                            AuditSubQuestion auditSubQuestion2 = new AuditSubQuestion();
                                                            auditSubQuestion2.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                            auditSubQuestion2.setmStrAuditId(mAuditId);
                                                            auditSubQuestion2.setmStrServerId(mStrSubQuestionId2);
                                                            auditSubQuestion2.setmStrQuestionTxt(mStrSubQuestion2);
                                                            if (mStrSubQuestionAns2.contains("\\r\\n")) {
                                                                auditSubQuestion2.setmStrAnswerOption(mStrSubQuestionAns2.replace("\\r\\n", "\r\n"));
                                                            }
                                                            auditSubQuestion2.setmStrAnswerOption(mStrSubQuestionAns2.replace("\r\n", "#"));
                                                            auditSubQuestion2.setmStrAnswerOptionId(mStrSubQuestionAnsID2.replace(",", "#"));
                                                            auditSubQuestion2.setmStrQuestionType(mStrSubAnsType2);
                                                            auditSubQuestion2.setmStrMainQuestion(mStrSubQuestionId + "");
                                                            auditSubQuestion2.setmStrQuestionFor(mStrQuestionCondition2);
                                                            auditSubQuestion2.setmStrMainLocation(mStrLocationId);
                                                            auditSubQuestion2.setmStrSubLocation(mStrSubCatID);
                                                            if (mArraySubQuestion2.length() > 0) {
                                                                auditSubQuestion2.setmStrSubQuestion("1");
                                                            } else {
                                                                auditSubQuestion2.setmStrSubQuestion("0");
                                                            }


                                                            if (mStrSubAnsType2.equals("5")) {

                                                            } else {
                                                                db.insert_tb_audit_sub_questions(auditSubQuestion2);
                                                            }


                                                        }
                                                    }


                                                }
                                            }

                                            JSONArray JAMeasurementQuestion = JOSubCategory.getJSONArray("measurement_questions");
                                            System.out.println("<><><><>JAMeasurementQuestion" + JAMeasurementQuestion.toString());
                                            for (int n = 0; n < JAMeasurementQuestion.length(); n++) {
                                                JSONObject JOMeasurementQuestion = JAMeasurementQuestion.getJSONObject(n);
                                                String mStrNormalQuestionID = JOMeasurementQuestion.getString("id");
                                                String mStrNormalQuestion = JOMeasurementQuestion.getString("question");
                                                String mStrNormalQuesAnswer = JOMeasurementQuestion.getString("answer");
                                                String mStrNormalAnswerID = JOMeasurementQuestion.getString("answernum");
                                                String mStrAnswerType = JOMeasurementQuestion.getString("type");
                                                JSONArray JASubQuestion = JOMeasurementQuestion.getJSONArray("sub_questions");

                                                JSONArray JAInspectorQuestion = JOMeasurementQuestion.getJSONArray("ins_questions");

                                                System.out.println("<><><><>JASubQuestion" + JASubQuestion.toString());
                                                AuditQuestion auditQuestion = new AuditQuestion();
                                                auditQuestion.setmStrAuditId(mAuditId);
                                                auditQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                auditQuestion.setmStrMainLocation(mStrLocationId + "");
                                                auditQuestion.setmStrSubLocation(mStrSubCatID + "");
                                                auditQuestion.setmStrQuestionTxt(mStrNormalQuestion);
                                                auditQuestion.setmStrServerId(mStrNormalQuestionID);
                                                auditQuestion.setmStrAnswerOption(mStrNormalQuesAnswer.replace("\r\n", "#"));
                                                auditQuestion.setmStrAnswerOptionId(mStrNormalAnswerID.replace(",", "#"));
                                                auditQuestion.setmStrQuestionType(mStrAnswerType);
                                                auditQuestion.setmStrQuestionStatus("0");
                                                if (JASubQuestion.length() > 0) {
                                                    auditQuestion.setmStrSubQuestion("1");
                                                } else {
                                                    auditQuestion.setmStrSubQuestion("0");
                                                }

                                                if (JAInspectorQuestion.length() > 0) {
                                                    auditQuestion.setmStrInspectorQuestion("1");
                                                } else {
                                                    auditQuestion.setmStrInspectorQuestion("0");

                                                }

                                                if (mStrAnswerType.equals("5")) {

                                                } else {
                                                    int measurementLocationLocalID = db.insert_tb_audit_question(auditQuestion);
                                                }


                                                for (int m = 0; m < JASubQuestion.length(); m++) {
                                                    JSONObject JOSubQuestion = JASubQuestion.getJSONObject(m);
                                                    String mStrSubQuestionId = JOSubQuestion.getString("id");
                                                    String mStrSubQuestion = JOSubQuestion.getString("question");
                                                    String mStrSubQuestionAns = JOSubQuestion.getString("answer");
                                                    String mStrSubQuestionAnsID = JOSubQuestion.getString("answernum");
                                                    String mStrQuestionCondition = JOSubQuestion.getString("answer_id");
                                                    String mStrSubAnsType = JOSubQuestion.getString("type");
                                                    JSONArray mArraySubQuestion = JOSubQuestion.getJSONArray("sub_questions");
                                                    AuditSubQuestion auditSubQuestion = new AuditSubQuestion();
                                                    auditSubQuestion.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                    auditSubQuestion.setmStrAuditId(mAuditId);
                                                    auditSubQuestion.setmStrServerId(mStrSubQuestionId);
                                                    auditSubQuestion.setmStrQuestionTxt(mStrSubQuestion);
                                                    auditSubQuestion.setmStrAnswerOption(mStrSubQuestionAns.replace("\r\n", "#"));
                                                    auditSubQuestion.setmStrAnswerOptionId(mStrSubQuestionAnsID.replace(",", "#"));
                                                    auditSubQuestion.setmStrQuestionType(mStrSubAnsType);
                                                    auditSubQuestion.setmStrMainLocation(mStrLocationId);
                                                    auditSubQuestion.setmStrMainQuestion(mStrNormalQuestionID + "");
                                                    auditSubQuestion.setmStrQuestionFor(mStrQuestionCondition);
                                                    auditSubQuestion.setmStrSubLocation(mStrSubCatID);
                                                    if (mArraySubQuestion.length() > 0) {
                                                        auditSubQuestion.setmStrSubQuestion("1");
                                                    } else {
                                                        auditSubQuestion.setmStrSubQuestion("0");
                                                    }


                                                    if (mStrSubAnsType.equals("5")) {

                                                    } else {
                                                        int lastSubQuestion = db.insert_tb_audit_sub_questions(auditSubQuestion);
                                                    }

                                                    if (mArraySubQuestion.length() > 0) {
                                                        for (int z = 0; z < mArraySubQuestion.length(); z++) {
                                                            JSONObject SubQuestion = mArraySubQuestion.getJSONObject(z);
                                                            String mStrSubQuestionId2 = SubQuestion.getString("id");
                                                            String mStrSubQuestion2 = SubQuestion.getString("question");
                                                            String mStrSubQuestionAns2 = SubQuestion.getString("answer");
                                                            String mStrSubQuestionAnsID2 = SubQuestion.getString("answernum");
                                                            String mStrQuestionCondition2 = SubQuestion.getString("answer_id");
                                                            String mStrSubAnsType2 = SubQuestion.getString("type");
                                                            JSONArray mArraySubQuestion2 = SubQuestion.getJSONArray("sub_questions");
                                                            AuditSubQuestion auditSubQuestion2 = new AuditSubQuestion();
                                                            auditSubQuestion2.setmStrUserId(PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                                                            auditSubQuestion2.setmStrAuditId(mAuditId);
                                                            auditSubQuestion2.setmStrServerId(mStrSubQuestionId2);
                                                            auditSubQuestion2.setmStrQuestionTxt(mStrSubQuestion2);
                                                            auditSubQuestion2.setmStrAnswerOption(mStrSubQuestionAns2.replace("\r\n", "#"));
                                                            auditSubQuestion2.setmStrAnswerOptionId(mStrSubQuestionAnsID2.replace(",", "#"));
                                                            auditSubQuestion2.setmStrQuestionType(mStrSubAnsType2);
                                                            auditSubQuestion2.setmStrMainQuestion(mStrSubQuestionId + "");
                                                            auditSubQuestion2.setmStrMainLocation(mStrLocationId);
                                                            auditSubQuestion2.setmStrSubLocation(mStrSubCatID);

                                                            auditSubQuestion2.setmStrQuestionFor(mStrQuestionCondition2);
                                                            if (mArraySubQuestion2.length() > 0) {
                                                                auditSubQuestion2.setmStrSubQuestion("1");
                                                            } else {
                                                                auditSubQuestion2.setmStrSubQuestion("0");
                                                            }

                                                            if (mStrSubAnsType2.equals("5")) {

                                                            } else {
                                                                db.insert_tb_audit_sub_questions(auditSubQuestion2);
                                                            }


                                                        }
                                                    }


                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            } else if (mStrStatus.equals("2")) {
                                //hidePDialog();
                                CommonUtils.showSessionExp(ActivitySelectCountryStandard.this);
                            } else {
                                //hidePDialog();
                                mShowAlert(response.getString("message"), ActivitySelectCountryStandard.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hidePDialog();
                        Toast.makeText(ActivitySelectCountryStandard.this, getString(R.string.mTextFile_error_something_went_wrong) + error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", PreferenceManager.getFormiiId(ActivitySelectCountryStandard.this));
                params.put("auth_token", PreferenceManager.getFormiiAuthToken(ActivitySelectCountryStandard.this));
                params.put("audit_id", mAuditId);
                params.put("lang", mStrLanguageCode);
                params.put("country_id", mStrCountryId);
                params.put("location_id", mStrLocationId);
                System.out.println("<><><>#" + params);
                return params;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strRequest);
    }*/


}