# lein-bump-version

[![Clojars Project](https://img.shields.io/clojars/v/lein-bump-version.svg)](https://clojars.org/lein-bump-version)

A Leiningen plugin to bump project version and project group dependencies to the same version.

## Usage

In `:plugins` in your `project.clj`:

```text
[lein-bump-version "0.1.5"]
```

Bump version for project and all dependencies of the project group:

```
$ lein bump-version 0.3.2
```

To increment the PATCH version of the project you may simply use:

```
$ lein bump-version
```

You may also use the plugin to update dependencies by their group:

```
$ lein bump-version 0.1.6 project.clj lein-bump-version
```

See your `project.clj` file update.

## License

Copyright © 2015 ViaSat

Copyright © 2018 Sergey Sobko

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
