# jisho-link-fetcher

This project is primarily to help with the creation of Anki decks for
[Pokemon Japanese School](https://github.com/Archenoth/Pokemon-Japanese-School).
It is a Clojure library made specifically to extract links in the following
form:

    [jisho:かわいい]

This is most common in [Org files](http://orgmode.org/): particularly
with a header argument like the following:

    #+LINK: jisho http://jisho.org/search?utf8=%E2%9C%93&keyword=

Which would make links like this automagically search Jisho for the
term:

    [[jisho:かわいい][かわいい]]

This library extracts all such links from a file, fetches the Kanji
representations and the definitions of the words, and then writes them
into a tab-delimited file that Anki can then import.

There is also a functions to pull the definition of a word right from
Jisho.

## Usage
To use this from the repository, you can dump a list of definitions
like so:

```bash
lein run /path/to/file/with/jisho/links anki.txt
```

You can also make yourself a jar to use anywhere by running:

```bash
lein uberjar
```

To use this as a library to pull Jisho definitions for Japanese words,
you can use the `jisho-definition` function in
`jisho-link-fetcher.core`:

```clojure
(jisho-definition "かわいい")
```

Which would return:

    {:word "可愛い", :definition "cute; adorable; charming; lovely; pretty"}

Or from English:
```clojure
(jisho-definition "Puppy")
```

Which returns:

    {:word "子犬", :definition "puppy"}

## License

Copyright © 2016 Archenoth

Distributed under the Eclipse Public License version 1.0
