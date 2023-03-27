package com.serverless.transformers

import com.serverless.models.responses.Response

interface Transformer<T> {
    fun transform(t: T?): Response?
}

interface BulkTransformer<T> {
    fun transform(t: Array<T>): Array<Response>
}