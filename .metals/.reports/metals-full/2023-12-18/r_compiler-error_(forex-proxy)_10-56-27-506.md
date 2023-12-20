file://<WORKSPACE>/Main.scala
### java.lang.NullPointerException

occurred in the presentation compiler.

action parameters:
offset: 9
uri: file://<WORKSPACE>/Main.scala
text:
```scala
object Ma@@

```



#### Error stacktrace:

```
java.base/java.util.Arrays.sort(Arrays.java:1441)
	scala.tools.nsc.classpath.JFileDirectoryLookup.listChildren(DirectoryClassPath.scala:118)
	scala.tools.nsc.classpath.JFileDirectoryLookup.listChildren$(DirectoryClassPath.scala:102)
	scala.tools.nsc.classpath.DirectoryClassPath.listChildren(DirectoryClassPath.scala:293)
	scala.tools.nsc.classpath.DirectoryClassPath.listChildren(DirectoryClassPath.scala:293)
	scala.tools.nsc.classpath.DirectoryLookup.list(DirectoryClassPath.scala:83)
	scala.tools.nsc.classpath.DirectoryLookup.list$(DirectoryClassPath.scala:78)
	scala.tools.nsc.classpath.DirectoryClassPath.list(DirectoryClassPath.scala:293)
	scala.tools.nsc.classpath.AggregateClassPath.$anonfun$list$3(AggregateClassPath.scala:106)
	scala.collection.immutable.Vector.foreach(Vector.scala:2124)
	scala.tools.nsc.classpath.AggregateClassPath.list(AggregateClassPath.scala:102)
	scala.tools.nsc.util.ClassPath.list(ClassPath.scala:34)
	scala.tools.nsc.util.ClassPath.list$(ClassPath.scala:34)
	scala.tools.nsc.classpath.AggregateClassPath.list(AggregateClassPath.scala:31)
	scala.tools.nsc.symtab.SymbolLoaders$PackageLoader.doComplete(SymbolLoaders.scala:297)
	scala.tools.nsc.symtab.SymbolLoaders$SymbolLoader.$anonfun$complete$2(SymbolLoaders.scala:249)
	scala.tools.nsc.symtab.SymbolLoaders$SymbolLoader.complete(SymbolLoaders.scala:247)
	scala.reflect.internal.Symbols$Symbol.completeInfo(Symbols.scala:1565)
	scala.reflect.internal.Symbols$Symbol.info(Symbols.scala:1537)
	scala.reflect.internal.Types$TypeRef.decls(Types.scala:2608)
	scala.tools.nsc.typechecker.Namers$Namer.enterPackage(Namers.scala:761)
	scala.tools.nsc.typechecker.Namers$Namer.dispatch$1(Namers.scala:297)
	scala.tools.nsc.typechecker.Namers$Namer.standardEnterSym(Namers.scala:310)
	scala.tools.nsc.typechecker.AnalyzerPlugins.pluginsEnterSym(AnalyzerPlugins.scala:496)
	scala.tools.nsc.typechecker.AnalyzerPlugins.pluginsEnterSym$(AnalyzerPlugins.scala:495)
	scala.meta.internal.pc.MetalsGlobal$MetalsInteractiveAnalyzer.pluginsEnterSym(MetalsGlobal.scala:68)
	scala.tools.nsc.typechecker.Namers$Namer.enterSym(Namers.scala:288)
	scala.tools.nsc.typechecker.Analyzer$namerFactory$$anon$1.apply(Analyzer.scala:50)
	scala.tools.nsc.Global$GlobalPhase.applyPhase(Global.scala:480)
	scala.tools.nsc.Global$Run.$anonfun$compileLate$2(Global.scala:1685)
	scala.tools.nsc.Global$Run.$anonfun$compileLate$2$adapted(Global.scala:1684)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:576)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:574)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1300)
	scala.tools.nsc.Global$Run.compileLate(Global.scala:1684)
	scala.tools.nsc.interactive.Global.parseAndEnter(Global.scala:668)
	scala.tools.nsc.interactive.Global.typeCheck(Global.scala:678)
	scala.meta.internal.pc.HoverProvider.typedHoverTreeAt(HoverProvider.scala:298)
	scala.meta.internal.pc.HoverProvider.hoverOffset(HoverProvider.scala:43)
	scala.meta.internal.pc.HoverProvider.hover(HoverProvider.scala:22)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$hover$1(ScalaPresentationCompiler.scala:331)
```
#### Short summary: 

java.lang.NullPointerException