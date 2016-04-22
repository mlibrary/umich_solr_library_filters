# umich_solr_library_filters

This package contains a number of solr analysis filters to transform library
data at index and/or query time in a solr index.

## Using and/or Building

To use the [provided `.jar` file](https://github.com/billdueber/umich_solr_library_filters/tree/master/dist/), just stick it 
into a place where solr will find it, and
create an appropriate `fieldType` definition, as shown below.

The jarfile provided is built on/against solr 6.x. To build it youself, simply check out this repo inside of the `solr/contrib/` directory and run (from `solr/`) `ant dist-contrib`. You'll still need to copy the jarfile to wherever you need it.

## LCCN Normalizer

The `LCCNNormalizer` will, as you might expect, normalize LCCNs (typically
found in MARC field `010`) according to the
[algorithm on the Library of Congress site](http://www.loc.gov/marc/lccn-namespace.html#syntax).

This filter presumes that whatever you're sending it is supposed to be an LCCN;
if you give it something else, what comes out the other end may or may not make
any sense. (This is primarily because it's hard to just look at a string and
determine if it's supposed to be an LCCN).

```xml

	<fieldType name="lccn_normalizer" class="solr.TextField">
		<analyzer>
			<tokenizer class="solr.KeywordTokenizerFactory"/>
			<filter class="edu.umich.lib.solr_filters.LCCNNormalizerFilterFactory"/>
		</analyzer>
	</fieldType>
```

## ISBN Normalizer

The `ISBNNormalizer` will take a token, attempt to extract something that
looks like a valid ISBN out of it, turn it into an ISBN-13 if need be, and
index the resulting 13-digit string.

Anything that doesn't seem to contain an ISBN will not index anything at all,
so if you want to be able to look for random strings that come out of your
`020` fields, you'll need to use an additional field for that.

```xml
<!-- In this example, allow multiple comma- or semi-colon-separated values -->
	<fieldType name="isbn" class="solr.TextField">
		<analyzer>
	        <tokenizer class="solr.PatternTokenizerFactory" pattern="[;,]\s*" />
			<filter class="edu.umich.lib.solr_filters.ISBNNormalizerFilterFactory"/>
		</analyzer>
	</fieldType>

```

## LC Shelf Key

This is a simple wrapper around the [solrmarc code for generating a sortable
shelfkey from an LC Classification Number](https://code.google.com/p/solrmarc/source/browse/trunk/lib/solrmarc/src/org/solrmarc/callnum/LCCallNumber.java).
You can see examples in the original code; I'm just wrapping it up as a filter.

```xml
	<fieldType name="lc_callnumber_shelfkey" class="solr.TextField">
		<analyzer>
			<tokenizer class="solr.KeywordTokenizerFactory"/>
			<filter class="edu.umich.lib.solr_filters.LCCallNumberShelfKeyFilterFactory"/>
		</analyzer>
	</fieldType>

```

