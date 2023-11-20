package org.publicvalue.multiplatform.oauth.screens

/**
 * Copied from https://github.com/realityexpander/NoteAppKMM
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class CommonParcelize()

// For Android @TypeParceler
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Retention(AnnotationRetention.SOURCE)
@Repeatable
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
expect annotation class CommonTypeParceler<T, P : CommonParceler<in T>>()

// For Android Parceler
expect interface CommonParceler<T>
