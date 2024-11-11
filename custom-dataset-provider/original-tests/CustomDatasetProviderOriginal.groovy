package com.morpheusdata.uthman

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.data.DatasetInfo
import com.morpheusdata.core.data.DatasetQuery
import com.morpheusdata.core.providers.AbstractDatasetProvider
import com.morpheusdata.uthman.utils.CustomApiServiceOriginal
import io.reactivex.rxjava3.core.Observable
import groovy.util.logging.Slf4j

@Slf4j
class CustomDatasetProviderOriginal extends AbstractDatasetProvider<Map, Long> {

    public static final providerName = 'Custom REST API Dataset Provider'
    public static final providerNamespace = 'custom'
    public static final providerKey = 'api'
    public static final providerDescription = 'A dataset of name-value pairs from a REST API'

    MorpheusContext morpheusContext
    Plugin plugin

    static customApiService

    CustomDatasetProviderOriginal(Plugin plugin, MorpheusContext morpheus) {
        this.plugin = plugin
        this.morpheusContext = morpheus
        this.customApiService = new CustomApiServiceOriginal()
    }

    @Override
    DatasetInfo getInfo() {
        new DatasetInfo(
            name: providerName,
            namespace: providerNamespace,
            key: providerKey,
            description: providerDescription
        )
    }

    /**
     * the key of this dataset
     * @return the key identifier used to access the dataset
     */
    @Override
    String getKey() {
        return providerKey
    }

    @Override
    Class<Map> getItemType() {
        return Map.class
    }

    @Override
    Observable<Map> list(DatasetQuery query) {
        String customEndpoint = query.parameters.get('endpoint')
        String customUrl = query.parameters.get('url')
        String KEY = query.parameters.get('nameKey')
        String VALUE = query.parameters.get('valueKey')
        String responseOnly = query.parameters.get('responseOnly')

        if (customEndpoint && customUrl) {
            def customAPI = customApiService.getDataFrom(customUrl, customEndpoint)
            if (customAPI.success) {
                if (responseOnly == 'true') {
                    return Observable.just(customAPI.data)
                }
                def results = []
                results <<
                        [
                                name: customAPI.data.get(KEY),
                                value: customAPI.data.get(VALUE)
                        ]
                return Observable.fromIterable(results)
            }
        }
        return Observable.empty()
    }

    @Override
    Observable<Map> listOptions(DatasetQuery query) {
        return list(query).map { [name: it.name, value: it.id] }
    }

    @Override
    Map fetchItem(Object value) {
        def rtn = null
        if(value instanceof Long) {
            rtn = item((Long)value)
        } else if(value instanceof CharSequence) {
            def longValue = value.isNumber() ? value.toLong() : null
            if(longValue) {
                rtn = item(longValue)
            }
        }
        return rtn
    }

    Map item(Long value) {
        def rtn = list(new DatasetQuery()).find{ it.id == value }
        return rtn
    }

    @Override
    String itemName(Map item) {
        return item.description
    }

    @Override
    Long itemValue(Map item) {
        return (Long)item.id
    }

    @Override
    boolean isPlugin() {
        return true
    }
}
