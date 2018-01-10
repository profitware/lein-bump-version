# lein-bump-version

[![Clojars Project](https://img.shields.io/clojars/v/lein-bump-version.svg)](https://clojars.org/lein-bump-version)

A Leiningen plugin to bump project version and project group dependencies to the same version.

## Usage

In `:plugins` in your `project.clj`:

```text
[lein-bump-version "0.1.2"]
```

Bump version for project and all dependencies of the project group:

```
$ lein bump-version project-name 0.3.2
```

See your `project.clj` file update.

## License

Copyright © 2015 ViaSat

Copyright © 2017 Sergey Sobko

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
