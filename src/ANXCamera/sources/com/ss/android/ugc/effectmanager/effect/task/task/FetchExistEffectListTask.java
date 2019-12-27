package com.ss.android.ugc.effectmanager.effect.task.task;

import android.os.Handler;
import android.text.TextUtils;
import com.ss.android.ugc.effectmanager.common.ErrorConstants;
import com.ss.android.ugc.effectmanager.common.listener.ICache;
import com.ss.android.ugc.effectmanager.common.listener.IJsonConverter;
import com.ss.android.ugc.effectmanager.common.task.ExceptionResult;
import com.ss.android.ugc.effectmanager.common.task.NormalTask;
import com.ss.android.ugc.effectmanager.common.utils.EffectCacheKeyGenerator;
import com.ss.android.ugc.effectmanager.context.EffectContext;
import com.ss.android.ugc.effectmanager.effect.model.Effect;
import com.ss.android.ugc.effectmanager.effect.model.EffectCategoryModel;
import com.ss.android.ugc.effectmanager.effect.model.EffectCategoryResponse;
import com.ss.android.ugc.effectmanager.effect.model.EffectChannelModel;
import com.ss.android.ugc.effectmanager.effect.model.EffectChannelResponse;
import com.ss.android.ugc.effectmanager.effect.task.result.EffectChannelTaskResult;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FetchExistEffectListTask extends NormalTask {
    private List<Effect> allDownloadedCategoryEffects;
    private ICache mCache = this.mEffectContext.getEffectConfiguration().getCache();
    private EffectContext mEffectContext;
    private IJsonConverter mJsonConverter = this.mEffectContext.getEffectConfiguration().getJsonConverter();
    private String panel;

    public FetchExistEffectListTask(String str, String str2, EffectContext effectContext, Handler handler) {
        super(handler, str2);
        this.panel = str;
        this.mEffectContext = effectContext;
    }

    private EffectChannelModel getCachedChannelModel() {
        InputStream queryToStream = this.mCache.queryToStream(EffectCacheKeyGenerator.generatePanelKey(this.mEffectContext.getEffectConfiguration().getChannel(), this.panel));
        return queryToStream != null ? (EffectChannelModel) this.mJsonConverter.convertJsonToObj(queryToStream, EffectChannelModel.class) : new EffectChannelModel();
    }

    private List<Effect> getCategoryAllEffects(List<String> list) {
        ArrayList arrayList = new ArrayList();
        for (String next : list) {
            for (Effect next2 : this.allDownloadedCategoryEffects) {
                if (TextUtils.equals(next, next2.getEffectId())) {
                    arrayList.add(next2);
                }
            }
        }
        return arrayList;
    }

    private List<EffectCategoryResponse> getCategoryEffectResponse(EffectChannelModel effectChannelModel) {
        List<EffectCategoryModel> category = effectChannelModel.getCategory();
        ArrayList arrayList = new ArrayList();
        for (EffectCategoryModel next : category) {
            if (next.checkValued()) {
                EffectCategoryResponse effectCategoryResponse = new EffectCategoryResponse(next.getId(), next.getName(), next.getKey(), getCategoryAllEffects(next.getEffects()), next.getTags(), next.getTagsUpdateTime());
                effectCategoryResponse.setCollectionEffect(effectChannelModel.getCollection());
                arrayList.add(effectCategoryResponse);
            }
        }
        return arrayList;
    }

    private List<Effect> getDownloadedEffectList(List<Effect> list) {
        ArrayList arrayList = new ArrayList();
        for (Effect next : list) {
            if (this.mCache.has(next.getId())) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public void execute() {
        if (TextUtils.isEmpty(this.panel)) {
            sendMessage(14, new EffectChannelTaskResult(new EffectChannelResponse(this.panel), new ExceptionResult((int) ErrorConstants.CODE_PANEL_NULL)));
            return;
        }
        EffectChannelResponse effectChannelResponse = new EffectChannelResponse();
        EffectChannelModel cachedChannelModel = getCachedChannelModel();
        if (cachedChannelModel == null) {
            sendMessage(14, new EffectChannelTaskResult(new EffectChannelResponse(this.panel), new ExceptionResult((int) ErrorConstants.CODE_INVALID_EFFECT_CACHE)));
        } else if (!cachedChannelModel.checkValued()) {
            sendMessage(14, new EffectChannelTaskResult(new EffectChannelResponse(this.panel), (ExceptionResult) null));
        } else {
            this.allDownloadedCategoryEffects = getDownloadedEffectList(cachedChannelModel.getEffects());
            if (this.allDownloadedCategoryEffects.isEmpty()) {
                sendMessage(14, new EffectChannelTaskResult(new EffectChannelResponse(this.panel), (ExceptionResult) null));
                return;
            }
            effectChannelResponse.setAllCategoryEffects(this.allDownloadedCategoryEffects);
            effectChannelResponse.setCategoryResponseList(getCategoryEffectResponse(cachedChannelModel));
            effectChannelResponse.setPanel(this.panel);
            effectChannelResponse.setPanelModel(cachedChannelModel.getPanel());
            sendMessage(14, new EffectChannelTaskResult(effectChannelResponse, (ExceptionResult) null));
        }
    }
}
