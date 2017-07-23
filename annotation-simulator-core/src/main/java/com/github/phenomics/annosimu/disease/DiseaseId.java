package com.github.phenomics.annosimu.disease;

import java.io.Serializable;

import com.google.common.collect.ComparisonChain;

/**
 * Representation of a disease from a database.
 *
 * <p>
 * The disease entity is defined as the combination of a database (OMIM, DECIPHER) and the
 * identifier inside this database.
 * </p>
 *
 * <p>
 * This class is immutable.
 * </p>
 *
 * @author <a href="mailto:sebastian.koehler@charite.de">Sebastian Koehler</a>
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public final class DiseaseId implements Comparable<DiseaseId>, Serializable {

  /** ID for serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Separator between disease database and disease id.
   */
  public static final String diseaseIdSeparator = ":";

  /** Disease database identifier. */
  private final DiseaseDatabase diseaseDb;

  /** The identifier of the disease in the DB. */
  private final String diseaseId;

  /**
   * Construct from String {@code "${DB}:${ID}"}.
   *
   * @throws IllegalArgumentException if the given disease ID string value is not valid.
   */
  public static DiseaseId constructFromString(String strVal) {
    if (!strVal.contains(":")) {
      throw new IllegalArgumentException("Disease ID " + strVal + " does not contain a colon");
    }
    final String[] tokens = strVal.split(":", 2);
    System.err.println(strVal);
    return new DiseaseId(DiseaseDatabase.valueOf(tokens[0].trim().toUpperCase()), tokens[1]);
  }

  /**
   * Constructor.
   *
   * @param db {@link DiseaseDatabase} to use.
   * @param diseaseId The disease identifier as a string.
   */
  public DiseaseId(DiseaseDatabase db, String diseaseId) {
    this.diseaseDb = db;
    this.diseaseId = diseaseId;
  }

  /**
   * @return The database that this disease is from.
   */
  public DiseaseDatabase getDiseaseDb() {
    return diseaseDb;
  }

  /**
   * @return The local identifier of the disease in the database.
   */
  public String getDiseaseId() {
    return diseaseId;
  }

  @Override
  public String toString() {
    return "DiseaseId [diseaseDb=" + diseaseDb + ", diseaseId=" + diseaseId + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((diseaseDb == null) ? 0 : diseaseDb.hashCode());
    result = prime * result + ((diseaseId == null) ? 0 : diseaseId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DiseaseId other = (DiseaseId) obj;
    if (diseaseDb != other.diseaseDb) {
      return false;
    }
    if (diseaseId == null) {
      if (other.diseaseId != null) {
        return false;
      }
    } else if (!diseaseId.equals(other.diseaseId)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(DiseaseId that) {
    return ComparisonChain.start().compare(this.diseaseDb, that.diseaseDb)
        .compare(this.diseaseId, that.diseaseId).result();
  }

}
