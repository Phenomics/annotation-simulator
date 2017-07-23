package com.github.phenomics.annosimu.disease;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.google.common.collect.ImmutableList;

/**
 * Collected annotation information for a given disease.
 *
 * <p>
 * At the moment, the only supported annotations must be annotations with HPO terms.
 * </p>
 *
 * <p>
 * This class is immutable.
 * </p>
 *
 * @author <a href="mailto:sebastian.koehler@charite.de">Sebastian Koehler</a>
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public final class AnnotatedDisease implements Serializable {

  /** ID to use when serializing. */
  private static final long serialVersionUID = 1L;

  /** The disease identifier from the database. */
  private final DiseaseId diseaseId;

  /** The primary name of the disease. */
  private final String name;

  /** Alternative disease name. */
  private final ImmutableList<String> alternativeNames;

  /** Positive annotations for the disease. */
  private final ImmutableList<HpoDiseaseAnnotation> positiveAnnotations;

  /** Negative annotations for the disease. */
  private final ImmutableList<HpoDiseaseAnnotation> negativeAnnotations;

  /**
   * Constructor.
   *
   * <p>
   * The alternative disease names will be sorted on construction.
   * </p>
   *
   * @param diseaseId The disease ID.
   * @param name The name of the disease.
   * @param alternativeNames Alternative disease names.
   * @param positiveAnnotations Positive disease annotations.
   * @param negativeAnnotations Negative disease annotations.
   */
  public AnnotatedDisease(DiseaseId diseaseId, String name, Collection<String> alternativeNames,
      Collection<HpoDiseaseAnnotation> positiveAnnotations,
      Collection<HpoDiseaseAnnotation> negativeAnnotations) {
    this.diseaseId = diseaseId;
    this.name = name;
    this.alternativeNames = ImmutableList.sortedCopyOf(alternativeNames);
    this.positiveAnnotations = ImmutableList.copyOf(positiveAnnotations);
    this.negativeAnnotations = ImmutableList.copyOf(negativeAnnotations);
  }

  /**
   * @return The disease ID.
   */
  public DiseaseId getDiseaseId() {
    return diseaseId;
  }

  /**
   * @return The primary disease name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return The alternative disease names.
   */
  public ImmutableList<String> getAlternativeNames() {
    return alternativeNames;
  }

  /**
   * @return Positive disease annotations.
   */
  public ImmutableList<HpoDiseaseAnnotation> getPositiveAnnotations() {
    return positiveAnnotations;
  }

  /**
   * @return Negative disease annotations.
   */
  public ImmutableList<HpoDiseaseAnnotation> getNegativeAnnotations() {
    return negativeAnnotations;
  }

  /**
   * Convenience method to obtain all positively associated {@link TermId}s.
   *
   * @return {@link List} with all associated terms.
   */
  public List<TermId> getAllAssociatedTermIds() {
    return positiveAnnotations.stream().map(a -> a.getTermId()).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "DiseaseAnnotation [diseaseId=" + diseaseId + ", name=" + name + ", alternativeNames="
        + alternativeNames + ", positiveAnnotations=" + positiveAnnotations
        + ", negativeAnnotations=" + negativeAnnotations + "]";
  }

}
