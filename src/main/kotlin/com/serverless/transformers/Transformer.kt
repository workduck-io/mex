package com.serverless.transformers

import com.serverless.models.Response

interface Transformer<T> {
    fun transform(t: T?): Response?
}
