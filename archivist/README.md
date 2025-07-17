# Baldur's Archivist

Java library for dealing with various BG3 file formats:  .pkg, .lsf, etc.

FYI this library was really hacked together. ]
* I ported most of this code from a C# project called [LSLib](https://github.com/Norbyte/lslib) with the help of [Cursor's AI](http://cursor.com).
* LZ4 compression wasn't supported very well in Java, so this library requires that lz4 be installed in your container/machine.  `apt install lz4`.  My code does a process call to it.  Not pretty, but I didn't feel like figuring out what exactly wasn't supported by the LZ4 libraries out there.

`
   <dependency>
      <groupId>org.baldurs.forge</groupId>
      <artifactId>baldurs-archivist</artifactId>
    </dependency>
`