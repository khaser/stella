# Stella typechecker

## Build & run

Build:
```
./gradlew build
```

Parse a file:
```bash
./gradlew run --args="program.stella"
```


## Implemented extensions

* TODO: core language
* TODO: `#unit-type`
* TODO: `#pairs`
* TODO: `#tuples`
* TODO: `#records`
* TODO: `#let-bindings`
* TODO: `#type-ascriptions`
* TODO: `#sum-types`
* TODO: `#lists`
* TODO: `#variants`
* TODO: `#fixpoint-combinator`

## Testing
Test suite has been borrowed from [qexik0/stella-typechecker repo](https://github.com/qexik0/stella-typechecker/tree/main).
All tests can be runned with:

```
./gradlew test
```

Specific test group can be runned with:
```
./gradlew test --tests "<CoreTest|PairsRecordsTest|...>"
```

Supported test groups can be found in `src/main/test/java`.
