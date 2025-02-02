/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.solver.prepared.binder

import androidx.room.compiler.codegen.XPropertySpec
import androidx.room.compiler.codegen.XTypeName
import androidx.room.solver.CodeGenScope
import androidx.room.solver.prepared.result.PreparedQueryResultAdapter

/**
 * Connects a prepared query (INSERT, DELETE or UPDATE), db and ResultAdapter.
 *
 * Default implementation is [InstantPreparedQueryResultBinder]. If the query is deferred rather
 * than executed directly then alternative implementations can be implement using this interface
 * (e.g. Rx, ListenableFuture).
 */
abstract class PreparedQueryResultBinder(val adapter: PreparedQueryResultAdapter?) {

    /**
     * Receives the SQL and a function to bind args into a statement, it must then generate the code
     * that steps on the query and if applicable returns the result of the write operation.
     */
    abstract fun executeAndReturn(
        sqlQueryVar: String,
        dbProperty: XPropertySpec,
        bindStatement: CodeGenScope.(String) -> Unit,
        returnTypeName: XTypeName,
        scope: CodeGenScope
    )
}
