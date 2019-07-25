package com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.model;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.ibetter.www.adskitedigi.adskitedigi.R;
import com.ibetter.www.adskitedigi.adskitedigi.settings.audio_settings.AudioSettingsConstants;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.SignageMgrAccessModel;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.ToggleSMServices;
import com.ibetter.www.adskitedigi.adskitedigi.settings.signage_manager_settings.enter_prise_mode.EnterPriseSettingsModel;

import org.json.JSONException;
import org.json.JSONObject;

public class HandlePlayerCommands
{
    private Context context;
    public HandlePlayerCommands(Context context)
    {
        this.context = context;
    }

    public void handlePlayerSettingsRequest(JSONObject jsonObject) throws JSONException
    {
        String action = jsonObject.getString("action");
        if(action.equalsIgnoreCase(context.getString(R.string.ss_mode_settings_request)))
        {
            prepareSSModeSettingsRequest(jsonObject.getString(
                    context.getString(R.string.save_ftp_command_response_to_json_key)));

        }
        else if(action.equalsIgnoreCase(context.getString(R.string.ss_mode_change_request)))
        {

            String mode=jsonObject.getString(context.getString(R.string.ss_sm_mode_json_key));
            String serviceId = null;
            if(jsonObject.has(context.getString(R.string.signage_mgr_access_service_id)))
            {
                serviceId = jsonObject.getString(context.getString(R.string.signage_mgr_access_service_id));
            }
            changeSSConnectionMode(mode,serviceId);

        }else if(action.equalsIgnoreCase(context.getString(R.string.play_offer_text_action)))
        {
            AudioSettingsConstants.updateBgAudioSettings(context,jsonObject.getBoolean(context.getString(R.string.play_audio)));

        }else if(action.equalsIgnoreCase(context.getString(R.string.get_player_volume_request_json_key)))
        {
            preparePlayerVolumeRequestJSON(jsonObject.getString(
                    context.getString(R.string.save_ftp_command_response_to_json_key)));

        }else if(action.equalsIgnoreCase(context.getString(R.string.device_volume_change_request)))
        {
            updatePlayerVolume(jsonObject.getInt(
                    context.getString(R.string.volume_json_key)));
        }
    }

    private void prepareSSModeSettingsRequest(String saveResponseTo) throws JSONException
    {
        String mode= SignageMgrAccessModel.getSelectedSMMode(context);
        JSONObject requestObj= new JSONObject();
        requestObj.put(context.getString(R.string.ss_sm_mode_json_key),mode);
        EnterPriseSettingsModel.saveSMFTPResponse(context,requestObj.toString(),saveResponseTo);
    }

    private void changeSSConnectionMode(String mode,String serviceId)
    {
        Intent intent=new Intent(context,ToggleSMServices.class);
        intent.putExtra("switch_to",mode);
        if(serviceId!=null)
        {
            intent.putExtra("service_id",serviceId);
        }
        context.startService(intent);
    }

    private void preparePlayerVolumeRequestJSON(String saveResponseTo) throws JSONException
    {


        JSONObject infoJSON = new JSONObject();
        infoJSON.put(context.getString(R.string.volume_json_key),getVolumePercentage());

        EnterPriseSettingsModel.saveSMFTPResponse(context,infoJSON.toString(),saveResponseTo);
    }

    private int getVolumePercentage()
    {
        final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return Math.round((currentVolume*100.00f)/maxVolume);
    }

    private int getVolumeStreamIndex(int volume)
    {
        final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolumeStream = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return Math.round((volume*maxVolumeStream)/100.00f);
    }

    private void updatePlayerVolume(int volume)
    {
        final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getVolumeStreamIndex(volume), AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_RING,getVolumeStreamIndex(volume),0);

    }


}
