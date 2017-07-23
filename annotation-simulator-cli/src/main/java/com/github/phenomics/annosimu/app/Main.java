package com.github.phenomics.annosimu.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.phenomics.annosimu.disease.AnnotatedDisease;
import com.github.phenomics.annosimu.disease.AnnotatedDiseaseBuilder;
import com.github.phenomics.annosimu.disease.DiseaseDatabase;
import com.github.phenomics.annosimu.disease.DiseaseId;
import com.github.phenomics.annosimu.simulate.AnnotationSetModifier;
import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.io.base.TermAnnotationParserException;
import com.github.phenomics.ontolib.io.obo.hpo.HpoDiseaseAnnotationParser;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import com.github.phenomics.ontolib.ontology.data.TermId;

/**
 * Main entry point of the CLI application.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class Main {

  /** Command line arguments. */
  private String[] args;

  /** Configuration to use. */
  private Options options = new Options();

  /** The HPO {@link Ontology} to use. */
  private HpoOntology hpoOntology;

  /** The disease annotations to use. */
  private Map<DiseaseId, AnnotatedDisease> diseaseAnnotations;

  /**
   * Constructor.
   *
   * @param args Command line arguments to use.
   */
  public Main(String[] args) {
    this.args = args;
  }

  /**
   * Run the CLI application.
   *
   * @throws RuntimeExceptions if an error occurs.
   */
  public void run() {
    parseOptions();

    System.err.println("Patient Simulator\n");
    System.err.println("Options\n");
    System.err.println(options.toString());

    loadOntology();
    loadDiseaseAnnotations();
    performSimulation();
  }

  /**
   * Load HPO from OBO file and update {@link #hpoOntology};
   *
   * @throws RuntimeException in the case of problems with loading the HPO OBO file.
   */
  private void loadOntology() {
    final HpoOboParser parser = new HpoOboParser(options.getPathHpoObo());
    try {
      this.hpoOntology = parser.parse();
    } catch (IOException e) {
      throw new RuntimeException("Could not load HPO OBO file " + options.getPathHpoObo(), e);
    }
  }

  /**
   * Load disease annotations from file and set {@link #diseaseAnnotations}.
   */
  private void loadDiseaseAnnotations() {
    // TODO: this should go into ontolib-io!
    Map<DiseaseId, AnnotatedDiseaseBuilder> builders = new HashMap<>();
    try (final HpoDiseaseAnnotationParser annoParser =
        new HpoDiseaseAnnotationParser(options.getPathHpoDiseaseAnnotation())) {
      while (annoParser.hasNext()) {
        final HpoDiseaseAnnotation anno = annoParser.next();
        final DiseaseId diseaseId =
            new DiseaseId(DiseaseDatabase.valueOf(anno.getDb()), anno.getDbObjectId());
        if (!builders.containsKey(diseaseId)) {
          builders.put(diseaseId, new AnnotatedDiseaseBuilder());
        }
        builders.get(diseaseId).processAnnotation(anno);
      }
    } catch (IOException e) {
      throw new RuntimeException("There was a problem with reading the HPO OBO file "
          + options.getPathHpoDiseaseAnnotation(), e);
    } catch (TermAnnotationParserException e) {
      throw new RuntimeException("There was a problem with parsing the HPO OBO file "
          + options.getPathHpoDiseaseAnnotation(), e);
    }

    // Build the actual objects.
    diseaseAnnotations = new HashMap<>();
    for (Entry<DiseaseId, AnnotatedDiseaseBuilder> entry : builders.entrySet()) {
      diseaseAnnotations.put(entry.getKey(), entry.getValue().build());
    }
  }

  /**
   * Parse command line options and update {@link #options}.
   */
  private void parseOptions() {
    // TODO: Actually parse something.
    options.setDiseaseId("DECIPHER:2");
    options.setPathHpoObo(new File("C:\\Users\\mholtgre\\Downloads\\ontology\\hp.obo"));
    options.setPathHpoDiseaseAnnotation(
        new File("C:\\Users\\mholtgre\\Downloads\\ontology\\phenotype_annotation.tab"));
  }

  /**
   * @return {@link DiseaseId} to use, can be sampled at random if given as "*:RANDOM".
   */
  private DiseaseId getDiseaseId() {
    if (options.getDiseaseId().endsWith(":RANDOM")) {
      final Random rng = new Random(options.getSeed());
      final List<DiseaseId> diseases = new ArrayList<>(diseaseAnnotations.keySet());
      return diseases.get(rng.nextInt(diseases.size()));
    } else {
      return DiseaseId.constructFromString(options.getDiseaseId());
    }
  }

  /**
   * Perform the actual simulation of phenotype annotations.
   */
  private void performSimulation() {
    // Construct the query simulator to use.
    final AnnotationSetModifier<HpoTerm, HpoTermRelation> queryModifier =
        new AnnotationSetModifier<>(hpoOntology.getPhenotypicAbnormalitySubOntology(),
            new AnnotationSetModifier.Options(options.getSeed(), options.getMinQuerySize(),
                options.getMaxQuerySize(), options.getNoiseFraction(),
                options.getMapUpProbability()));
    // Get DiseaseId and perform simulation.
    final DiseaseId diseaseId = getDiseaseId();
    final AnnotatedDisease diseaseAnnotation = diseaseAnnotations.get(diseaseId);
    List<TermId> simulatedTermIds = queryModifier.simulateAnnotationSet(diseaseAnnotation);
    Collections.sort(simulatedTermIds);

    // Print simulated list of term ids.
    System.err.println("disease ID: " + diseaseId);
    System.err.println("disease name: " + diseaseAnnotations.get(diseaseId).getName());
    System.err.println("original terms: " + diseaseAnnotation.getAllAssociatedTermIds().stream()
        .map(tId -> tId.getIdWithPrefix()).collect(Collectors.toList()));
    System.err
        .println("original terms names: " + diseaseAnnotation.getAllAssociatedTermIds().stream()
            .map(tId -> hpoOntology.getTermMap().get(tId).getName()).collect(Collectors.toList()));
    System.err.println("simulated terms: "
        + simulatedTermIds.stream().map(tId -> tId.getIdWithPrefix()).collect(Collectors.toList()));
    System.err.println("simulated term names: " + simulatedTermIds.stream()
        .map(tId -> hpoOntology.getTermMap().get(tId).getName()).collect(Collectors.toList()));
  }

  public static void main(String[] args) {
    try {
      new Main(args).run();
    } catch (RuntimeException e) {
      System.err.println("An error occured!");
      System.err.println();
      System.err.println("The error message is given below");
      System.err.println();
      System.err.println("    " + e.getMessage());
      System.err.println();
      System.err.println("Below, you can find a detailed error message that can be helpful for");
      System.err.println("the developers to find the bug.");
      System.err.println();
      System.err.println("=====================================================================");
      System.err.println();
      e.printStackTrace();
    }
  }

}
