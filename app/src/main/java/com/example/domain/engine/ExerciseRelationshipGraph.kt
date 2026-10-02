package com.example.domain.engine

import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.ExerciseRelationshipEntity
import com.example.data.local.model.RelationshipType

class ExerciseRelationshipGraph(
    private val allExercises: List<ExerciseEntity>,
    private val relationships: List<ExerciseRelationshipEntity>
) {
    private val exerciseMap = allExercises.associateBy { it.id }

    fun getExercise(id: String): ExerciseEntity? = exerciseMap[id]

    fun getRegressions(exerciseId: String): List<ExerciseEntity> {
        val relIds = relationships
            .filter { it.sourceExerciseId == exerciseId && it.relationshipType == RelationshipType.REGRESSION_OF.name }
            .map { it.targetExerciseId }
        val fallback = exerciseMap[exerciseId]?.regressionExerciseId
        val allIds = (relIds + listOfNotNull(fallback)).distinct()
        return allIds.mapNotNull { exerciseMap[it] }
    }

    fun getProgressions(exerciseId: String): List<ExerciseEntity> {
        val relIds = relationships
            .filter { it.sourceExerciseId == exerciseId && it.relationshipType == RelationshipType.PROGRESSION_OF.name }
            .map { it.targetExerciseId }
        val fallback = exerciseMap[exerciseId]?.progressionExerciseId
        val allIds = (relIds + listOfNotNull(fallback)).distinct()
        return allIds.mapNotNull { exerciseMap[it] }
    }

    fun getPrerequisites(exerciseId: String): List<ExerciseEntity> {
        return relationships
            .filter { it.sourceExerciseId == exerciseId && it.relationshipType == RelationshipType.PREREQUISITE_FOR.name }
            .mapNotNull { exerciseMap[it.targetExerciseId] }
    }

    fun getAlternatives(exerciseId: String): List<ExerciseEntity> {
        val relIds = relationships
            .filter {
                (it.sourceExerciseId == exerciseId || it.targetExerciseId == exerciseId) &&
                        it.relationshipType == RelationshipType.ALTERNATIVE_TO.name
            }
            .map { if (it.sourceExerciseId == exerciseId) it.targetExerciseId else it.sourceExerciseId }
        return relIds.distinct().mapNotNull { exerciseMap[it] }
    }

    fun getOverlapping(exerciseId: String): List<ExerciseEntity> {
        val relIds = relationships
            .filter {
                (it.sourceExerciseId == exerciseId || it.targetExerciseId == exerciseId) &&
                        it.relationshipType == RelationshipType.OVERLAPS_WITH.name
            }
            .map { if (it.sourceExerciseId == exerciseId) it.targetExerciseId else it.sourceExerciseId }
        return relIds.distinct().mapNotNull { exerciseMap[it] }
    }

    fun getComplements(exerciseId: String): List<ExerciseEntity> {
        return relationships
            .filter { it.sourceExerciseId == exerciseId && it.relationshipType == RelationshipType.COMPLEMENTS.name }
            .mapNotNull { exerciseMap[it.targetExerciseId] }
    }
}
