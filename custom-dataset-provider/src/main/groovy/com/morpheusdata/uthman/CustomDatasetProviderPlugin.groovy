package com.morpheusdata.uthman

import com.morpheusdata.core.Plugin

class CustomDatasetProviderPlugin extends Plugin {

    void initialize() {
        this.registerProvider(new CustomDatasetProvider(this, this.morpheus))
    }

    @Override
    void onDestroy() {
        // Cleanup logic
    }

    @Override
    String getName() {
        return "Custom REST API Dataset Provider Plugin"
    }
    @Override
    String getCode() {
        return "custom-rest-api-dataset-provider-plugin"
    }
}
