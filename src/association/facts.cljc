(ns association.facts
  "Industry rule/policy-statement catalog for the American Farm Bureau
  Federation (AFBF, Wikidata Q4743741) -- a 30th industry-association-
  level source (see cloud-itonami-assoc-6419-jpn-zenginkyo, -6512-jpn-sonpo,
  -6612-jpn-jsda, -6419-deu-bankenverband, -6612-usa-finra, -6512-usa-naic,
  -6920-jpn-jicpa, -6920-usa-aicpa, -6419-fra-fbf, -6511-jpn-seiho,
  -6910-jpn-nichibenren, -6810-jpn-recaj, -6411-jpn-boj, -6120-usa-ctia,
  -5110-usa-a4a, -3510-usa-eei, -2910-deu-vda, -5510-usa-ahla,
  -2100-usa-phrma, -4719-usa-nrf, -4100-usa-agc, -6020-usa-nab,
  -3600-usa-awwa, -4923-usa-ata, -5610-usa-nra, -2011-usa-acc,
  -8621-usa-ama, -6201-usa-gtia, -0610-usa-api for the first
  twenty-nine) per ADR-2607141700 (cloud-itonami-compliance-fact-federation).
  The FIRST entry aligned to ISIC 0150 (mixed farming) -- a new
  industry code for this family, chosen as the closest single
  representative class for a federation spanning many crop and
  livestock types (no broader 'general farming' class exists in
  ISIC Rev.4). A rule not in this table has NO spec-basis, full stop;
  extend `catalog`, do not invent an id/url/date.

  Both entries were directly WebFetch-verified against fb.org's own
  pages. 'Who We Are' states the exact founding date (November 12,
  1919, Chicago) in its own text. 'What We Do' describes AFBF's
  advocacy function but states no specific policy-adoption date of its
  own (only repeats the 1919 founding year) -- several attempts to
  find a second, independently-dated fb.org document (recent
  convention news releases, policy-book PDFs) hit HTTP 404/403 -- so
  :association-rule/established-date is deliberately omitted for this
  second entry rather than duplicating or fabricating a date.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"afbf"
   [{:association-rule/id "afbf.who-we-are"
     :association-rule/title "Who We Are"
     :association-rule/association "afbf"
     :association-rule/isic "0150"
     :association-rule/country "USA"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.fb.org/about/who-we-are"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "1919-11-12"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:governance}}
    {:association-rule/id "afbf.what-we-do"
     :association-rule/title "What We Do"
     :association-rule/association "afbf"
     :association-rule/isic "0150"
     :association-rule/country "USA"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.fb.org/about/what-we-do"
     :association-rule/url-provenance :official-association-site
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:advocacy}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-0150-usa-afbf Wave 0 (ADR-2607141700): "
                 (count (get catalog "afbf")) " afbf entries seeded with an "
                 "official fb.org citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
