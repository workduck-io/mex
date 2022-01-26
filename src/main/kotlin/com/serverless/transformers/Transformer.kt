package com.serverless.transformers

import com.serverless.models.responses.Response

interface Transformer<T> {
    fun transform(t: T?): Response?
}
