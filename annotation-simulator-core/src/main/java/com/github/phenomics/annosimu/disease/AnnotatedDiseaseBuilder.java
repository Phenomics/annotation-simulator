package com.github.phenomics.annosimu.disease;

import java.util.ArrayList;
import java.util.List;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.ontology.data.TermId;

// TODO: logic for checking for duplicates not transferred, should go to other class
// TODO: logic for writing out to TSV file not transffered, should go to other class

/**
 * Builder class for immutable {@link AnnotatedDisease}.
 *
 * @see #processAnnotation(HpoDiseaseAnnotation)
 *
 * @author <a href="mailto:sebastian.koehler@charite.de">Sebastian Koehler</a>
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public final class AnnotatedDiseaseBuilder {

  /** The disease identifier from the database. */
  private DiseaseId diseaseId;

  /** The primary name of the disease. */
  private String name;

  /** Alternative disease name. */
  private List<String> alternativeNames = new ArrayList<>();

  /** Positive annotations for the disease. */
  private List<HpoDiseaseAnnotation> positiveAnnotations = new ArrayList<>();

  /** Negative annotations for the disease. */
  private List<HpoDiseaseAnnotation> negativeAnnotations = new ArrayList<>();

  /**
   * Default constructor.
   */
  public AnnotatedDiseaseBuilder() {}

  /**
   * Add a new {@link HpoDiseaseAnnotation} object with advanced logic.
   *
   * <p>
   * All the other functions for adding and setting are relatively "dumb" and directly work on the
   * members.
   * </p>
   *
   * <p>
   * In contrast, this function does a more advanced processing and can be used to fill a
   * {@link AnnotatedDiseaseBuilder} step by step from a list of positive and negative annotations.
   * It will ensure that only one annotation for each HPO {@link TermId} is created and set the
   * disease ID and name.
   * </p>
   *
   * @param anno The {@link HpoDiseaseAnnotation} to process.
   */
  public void processAnnotation(HpoDiseaseAnnotation anno) {
    if (diseaseId == null) {
      setDiseaseId(new DiseaseId(DiseaseDatabase.valueOf(anno.getDb()), anno.getDbObjectId()));
    } else {
      if (!diseaseId
          .equals(new DiseaseId(DiseaseDatabase.valueOf(anno.getDb()), anno.getDbObjectId()))) {
        throw new IllegalArgumentException("HpoDiseaseAnnotation has conflicting disease ID "
            + anno.getDbReference() + " vs. " + diseaseId);
      }
    }

    if (name == null) {
      setName(anno.getDbName());
    } else {
      if (!name.equals(anno.getDbName())) {
        throw new IllegalArgumentException("HpoDiseaseAnnotation has conflicting disease name "
            + anno.getDbName() + " vs. " + name);
      }
    }

    // TODO: what about alternative names?!
    // TODO: what about handling of "references" as Sebastian's code does?

    if ("NOT".equals(anno.getQualifier())) {
      negativeAnnotations.add(anno);
    } else {
      positiveAnnotations.add(anno);
    }
  }

  /**
   * Set the disease ID.
   *
   * @param diseaseId The value to use.
   */
  public void setDiseaseId(DiseaseId diseaseId) {
    this.diseaseId = diseaseId;
  }

  /**
   * Set the primary disease name.
   *
   * @param The value to use.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Add alternative disease name.
   *
   * @param name The name to add.
   */
  public void addAlternativeName(String name) {
    alternativeNames.add(name);
  }

  /**
   * Add positive disease annotation.
   *
   * @param anno The {@link HpoDiseaseAnnotation} to add.
   */
  public void addPositiveAnnotation(HpoDiseaseAnnotation anno) {
    this.positiveAnnotations.add(anno);
  }

  /**
   * Add positive disease annotation.
   *
   * @param anno The {@link HpoDiseaseAnnotation} to add.
   */
  public void addNegativeAnnotation(HpoDiseaseAnnotation anno) {
    this.negativeAnnotations.add(anno);
  }

  /**
   * @return Freshly constructed {@link AnnotatedDisease} object.
   */
  public AnnotatedDisease build() {
    return new AnnotatedDisease(diseaseId, name, alternativeNames, positiveAnnotations,
        negativeAnnotations);
  }

}
