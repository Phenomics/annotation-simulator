package com.github.phenomics.annosimu.simulate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.phenomics.annosimu.disease.AnnotatedDisease;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import com.github.phenomics.ontolib.ontology.data.Term;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermIds;
import com.github.phenomics.ontolib.ontology.data.TermRelation;
import com.google.common.collect.ImmutableList;

// TODO: Use more descriptive name.
// TODO: Add usage example.

/**
 * Random modification of queries, given a {@link Disease} description.
 *
 * <p>
 * Allows for the simulation using two sources of errors that are a model for incorrect phenotyping
 * by clinicians. First, with a certain probability, noise terms that describe phenotypical
 * abnormalities are added. Second, for each disease phenotype term, the term is moved upwards
 * towards the root with a certain probability. The number of phenotyping terms is selected
 * uniformly at random within a given range.
 * </p>
 *
 * <p>
 * No term will be mapped up to the root and also no term will be treated "specially", i.e., the
 * assumption is that we only have "homogenous" terms in the sense that we only operate of terms of
 * the same kind. The important case here is that we work on the phenotypical abnormality
 * subontology only.
 * </p>
 *
 * <p>
 * Such simulations are the state of the art for benchmarking phenotype matching algorithms.
 * </p>
 *
 * <h5>Thread Safety</h5>
 *
 * <p>
 * Using this class is not thread safe as there is only one random number generator per
 * {@link AnnotationSetModifier} object.
 * </p>
 *
 * @author <a href="mailto:sebastian.koehler@charite.de">Sebastian Koehler</a>
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public final class AnnotationSetModifier<T extends Term, R extends TermRelation> {

  /** The ontology that the term IDs refer into. */
  private final Ontology<T, R> ontology;

  /** The configuration of the query modification. */
  private final Options options;

  /** All non-obsolete {@link TermId}s. */
  private final ImmutableList<TermId> nonObsoleteTermIds;

  /**
   * Random number generator.
   *
   * <p>
   * This member is the main state of this class and makes it not thread-safe.
   * </p>
   */
  private final Random rng;

  /**
   * Constructor.
   *
   * <p>
   * Initialize the random number generator from the seed in {@code options}.
   * </p>
   *
   * @param ontology
   * @param options
   * @param nonObsoleteTermIds
   *
   * @throws IllegalArgumentException if the configuration in {@code options} is invalid.
   */
  public AnnotationSetModifier(Ontology<T, R> ontology, Options options) {
    this.ontology = ontology;
    this.options = options;
    this.options.check();
    this.nonObsoleteTermIds = ImmutableList.copyOf(ontology.getNonObsoleteTermIds());
    this.rng = new Random(options.getSeed());
  }

  /**
   * Simulate a perturbed annotation set with the algorithm outlined in the class description.
   *
   * @param annotatedDisease The disease annotation information to use.
   * @return {@link List} with perturbation of {@link annotatedTermIds}.
   */
  public List<TermId> simulateAnnotationSet(AnnotatedDisease annotatedDisease) {
    // Get annotated terms, filtered to those that have a non-obsolete entry in the ontology.
    final List<TermId> annotatedTermsIds =
        annotatedDisease.getAllAssociatedTermIds().stream().filter(termId -> {
          final T term = ontology.getTermMap().get(termId);
          return (term != null && !term.isObsolete());
        }).collect(Collectors.toList());

    // Now add imprecision through mapping up terms and adding noise terms.
    final List<TermId> impreciseTermIds = addNoiseTerms(mapUpTerms(annotatedTermsIds));

    // Finally, pick number of terms to use and then pick that number of terms.
    return subSampleTerms(impreciseTermIds);
  }

  /**
   * Add imprecision by mapping up terms.
   *
   * @param termIds The {@link TermIds} to map up in ontology.
   * @return The modified term ids.
   */
  private List<TermId> mapUpTerms(List<TermId> termIds) {
    if (options.getMapUpProbability() == 0.0) {
      return termIds; // short-circuit if disabled
    }

    // Construct result, TermId by TermId. We want to have no duplicates but keep the order, thus we
    // also need a result set.
    final Set<TermId> resultSet = new HashSet<>();
    final List<TermId> result = new ArrayList<>();
    for (TermId termId : termIds) {
      TermId newTermId = termId;
      if (rng.nextDouble() < options.getMapUpProbability()) {
        // Pick random ancestor, remove self and root from set.
        final List<TermId> ancestorTermIds =
            new ArrayList<>(ontology.getAllAncestorTermIds(termIds));
        ancestorTermIds.remove(termId);
        ancestorTermIds.remove(ontology.getRootTermId());
        if (!ancestorTermIds.isEmpty()) {
          newTermId = ancestorTermIds.get(rng.nextInt(ancestorTermIds.size()));
        }
      }
      if (!resultSet.contains(newTermId)) {
        result.add(newTermId);
        resultSet.add(newTermId);
      }
    }

    return result;
  }

  /**
   * Add imprecision by adding noise.
   *
   * @param termIds The {@link TermIds} to add noise to.
   * @return The modified term ids.
   */
  private List<TermId> addNoiseTerms(List<TermId> termIds) {
    if (options.getNoiseFraction() == 0.0) {
      return termIds; // short-circuit if disabled
    }

    // Allocate new list for building the result and compute number of terms to add. Add at least
    // one term.
    final List<TermId> result = new ArrayList<>(termIds);
    final int termsToAdd = Math.max(1, (int) (result.size() * options.getNoiseFraction()));
    final int targetSize = result.size() + termsToAdd;

    // Keep track of all ancestors that are already in the result, ensure that the root is removed.
    final Set<TermId> resultAncestors =
        new HashSet<>(ontology.getAllAncestorTermIds(termIds, false));
    resultAncestors.remove(ontology.getRootTermId());

    // Add random terms while avoiding duplicates.
    while (result.size() < targetSize) {
      final TermId candidate = nonObsoleteTermIds.get(rng.nextInt(nonObsoleteTermIds.size()));
      if (!resultAncestors.contains(candidate)) {
        result.add(candidate);
        resultAncestors.addAll(ontology.getAncestorTermIds(candidate, false));
      }
    }

    return result;
  }

  /**
   * Sample entries from {@code impreciseTermId}.
   *
   * @param termIds The {@link List} of {@link TermId}s to sample from.
   * @return Sub-sampled copy of {@link TermId}s.
   */
  private List<TermId> subSampleTerms(List<TermId> termIds) {
    final int numTerms = Math.max(termIds.size(), options.getMinQuerySize()
        + rng.nextInt(options.getMaxQuerySize() - options.getMinQuerySize()));
    final List<TermId> result = new ArrayList<>();
    while (result.size() < numTerms) {
      // Pick random entry from impreciseTermIds, then move to result.
      final int idx = rng.nextInt(termIds.size());
      result.add(termIds.get(idx));
      termIds.remove(idx);
    }
    return result;
  }

  /**
   * Configuration to use for the modifcation of queries.
   *
   * <p>
   * This class is immutable.
   * </p>
   */
  public final static class Options {

    /** The seed to use for random number generation. */
    private final long seed;

    /** Minimal number of HPO terms in query. */
    private final int minQuerySize;

    /** Maximal number of HPO terms in query. */
    private final int maxQuerySize;

    /** Fraction of noise to use in simulation. */
    private final double noiseFraction;

    /** Chance of being mapped up. */
    private final double mapUpProbability;

    /**
     * Constructor.
     *
     * @param seed The seed to use for random number generation.
     * @param minQuerySize Minimal query size to use.
     * @param maxQuerySize Maximal query size to use.
     * @param noiseFraction The fraction of noise terms.
     * @param mapUpProbability The probability for a term to be mapped up.
     */
    public Options(long seed, int minQuerySize, int maxQuerySize, double noiseFraction,
        double mapUpProbability) {
      this.seed = seed;
      this.minQuerySize = minQuerySize;
      this.maxQuerySize = maxQuerySize;
      this.noiseFraction = noiseFraction;
      this.mapUpProbability = mapUpProbability;
    }

    /**
     * Check configuration.
     *
     * @throws IllegalArgumentException if the configuration is invalid.
     */
    public void check() {
      if (minQuerySize > maxQuerySize) {
        throw new IllegalArgumentException(
            "minQuerySize <= maxQuerySize expected, got " + minQuerySize + " > " + maxQuerySize);
      }
      if (noiseFraction < 0.0 || noiseFraction > 1.0) {
        throw new IllegalArgumentException(
            "Noise fraction must be between 0.0 and 1.0, but got: " + noiseFraction);
      }
      if (mapUpProbability < 0.0 || mapUpProbability > 1.0) {
        throw new IllegalArgumentException(
            "Probability for mapping up must be between 0.0 and 1.0, but got: " + mapUpProbability);
      }
    }

    /**
     * @return The seed to use for random number generation.
     */
    public long getSeed() {
      return seed;
    }

    /**
     * @return The minimal query size.
     */
    public int getMinQuerySize() {
      return minQuerySize;
    }

    /**
     * @return The maximal query size.
     */
    public int getMaxQuerySize() {
      return maxQuerySize;
    }

    /**
     * @return The noise fraction.
     */
    public double getNoiseFraction() {
      return noiseFraction;
    }

    /**
     * @return The probability of a term to be mapped upwards.
     */
    public double getMapUpProbability() {
      return mapUpProbability;
    }

    @Override
    public String toString() {
      return "Options [seed=" + seed + ", minQuerySize=" + minQuerySize + ", maxQuerySize="
          + maxQuerySize + ", noiseFraction=" + noiseFraction + ", mapUpProbability="
          + mapUpProbability + "]";
    }

  }

}
