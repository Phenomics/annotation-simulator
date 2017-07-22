# Disease-Phenotype Annotation Simulator

This software package allows the simulation of HPO phenotype terms from diseases in an imprecise way.
This allows for simulating imprecise and noisy description of patient's phenotypes in the clinic as outlined in [Koehler et al. (2009)](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2756558/).

## In Brief

- **Language/Platform:** Java >=8
- **License:** 3-clause BSD
- **Authors:**
    - Sebastian Koehler
    - Max Schubach
    - Manuel Holtgrewe
- **Availability:**
    - Maven module `annotation-simulator-core` for using as a library.
    - Stand-alone command line interface `annotation-simulator-cli`.

## The General Idea

The general idea is best explained by citing [Koehler et al. (2009)](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2756558/).

> In clinical practice, patients can not only have signs and symptoms that are related to some underlying disorder but may also have unrelated clinical problems.
> We refer to this as "noise."
> In oder to simulate noise, we added again half as many noise terms to the terms selected from the underlying disease.
> That means that if the patient had nine features, we added four randomly selected terms.
> We ensured that the noise terms were not ancestors or descendents of the terms annotated to the disease or of each other.

> Another difficultly with clinical databases is that physicians may not choose the same phrase to describe some clinical anomaly as that which is used in the database.
> This may be because the physician is unaware of the correct terminology or because detailed laboratory or clinical investigations have yet to be performed and a clinical anomaly can only be described on a general level.
> We refer to this as "imprecision."
> When the imprecision mode was turned on, every feature of the patient was randomly replaced by one of its ancestors, except the root of the ontology (organ abnormality).

> When both "noise" and "imprecision" were applied, we first performed the imprecision step (which may lead to a reduced number of features of the patient, for instance, if two query terms are mapped to the same ancestor term) and afterwards applied the noise-step.
