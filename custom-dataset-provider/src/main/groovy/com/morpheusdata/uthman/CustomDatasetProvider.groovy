package com.morpheusdata.uthman

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.data.DatasetInfo
import com.morpheusdata.core.data.DatasetQuery
import com.morpheusdata.core.providers.AbstractDatasetProvider
import com.morpheusdata.core.util.ApiParameterMap
import com.morpheusdata.uthman.utils.CustomApiService
import io.reactivex.rxjava3.core.Observable
import groovy.util.logging.Slf4j

@Slf4j
class CustomDatasetProvider extends AbstractDatasetProvider<Map, Object> {

    public static final NAME = 'Custom REST API Dataset Provider'
    public static final NAMESPACE = 'custom'
    public static final KEY = 'api'
    public static final DESCRIPTION = 'A dataset of name-value pairs from a REST API'

    @Override
    DatasetInfo getInfo() {
        new DatasetInfo(
                name: NAME,
                namespace: NAMESPACE,
                key: KEY,
                description: DESCRIPTION
        )
    }

    CustomApiService customApiService
    MorpheusContext morpheusContext
    Plugin plugin

    CustomDatasetProvider(Plugin plugin, MorpheusContext morpheus) {
        this.morpheusContext = morpheus
        this.plugin = plugin
        this.customApiService = new CustomApiService(plugin, morpheus)
    }

    @Override
    Observable<Map> list(DatasetQuery query) {
        def params = query.parameters ?: [:]

        String url = params.get('url', null)
        String endpoint = params.get('endpoint', null)
        String method = params.get('method', 'GET')
        def ssl = params.get('ssl')
      //  Boolean morpheusApi = params.get('morpheus_api', false) as Boolean
        String bearer = params.get('bearer_token', null)
        String basic = params.get('basic', null)
        String datasetKey = params.get('dataset', null)
        String nameField = params.get('nameField', 'name')
        String valueField = params.get('valueField', 'value')
        Boolean test = params.get('test', false) as Boolean

        switch (true) {
            case test:
                return handleTestCase(params)
            case url && endpoint && method && ssl:
                return handleCustomApiCase(params)
            default:
                log.warn("Missing required parameters (url, endpoint, method) for custom API call.")
                return Observable.empty()
        }
    }

    /**
     * Handles the case when params.test is true.
     */
    Observable<Map> handleTestCase(Map params) {
        def TEST_RESPONSE = customApiService.test(params)
        def TEST_RESULTS = TEST_RESPONSE.data.collect { resultObject ->
            [
                    name : resultObject["${params?.nameField}"] ?: 'N/A',
                    value: resultObject["${params?.valueField}"] ?: 'N/A'
            ]
        }
        log.info("[CustomDatasetProvider.list()] parsedResults: ${TEST_RESULTS}")
        return Observable.fromIterable((List<Map>)TEST_RESULTS)
    }

    /**
     * Handles the case when params exist and call the custom API service.
     */
    Observable<Map> handleCustomApiCase(Map params) {
        log.info("API Parameters: ${params}")
        def RESPONSE = customApiService.getData(params)
        log.info("API Response: ${RESPONSE}")
        log.info("Dataset Key: ${params?.dataset}")
        log.info("Extracted Data: ${RESPONSE?.data}")

        def data = params.dataset ? RESPONSE?.data["${params?.dataset}"] : RESPONSE?.data
        def index = 0
        def RESULTS = data.collect { result ->
            try {
                log.info("Processing result: ${result}")
                def resultMap = [
                        name : result["${params?.nameField}"] ?: "RESULT-${index}",
                        value: result["${params?.valueField}"] ?: "$result"
                ]
                index++
                resultMap
            } catch (Exception e) {
                log.info("Error processing result: ${e.message}")
                throw e
            }
        }

        return Observable.fromIterable(RESULTS)
    }

    @Override
    Observable<Map> listOptions(DatasetQuery query) {
        return list(query)
    }

    @Override
    Map fetchItem(Object value) {
        return item(value)
    }

    Map item(Object value) {
        return list(new DatasetQuery()).find { it == value }.toMap()
    }

    /**
     * gets the name for an item
     * @param item an item
     * @return the corresponding name for the name/value pair list
     */
    @Override
    String itemName(Map item) {
        return item.user
    }

    /**
     * gets the value for an item
     * @param item an item
     * @return the corresponding value for the name/value pair list
     */
    @Override
    Object itemValue(Map item) {
        return item.type
    }

    @Override
    String getCode() {
        return "custom-rest-dataset-provider"
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    String getKey() {
        return KEY
    }

    @Override
    Class<Map> getItemType() {
        return Map.class
    }

    @Override
    boolean isPlugin() {
        return true
    }
}