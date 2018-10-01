.class public CodeGen/test
.super java/lang/Object
	
.field static i [F
	
	; standard class static initializer 
.method static <clinit>()V
	
	dup
	iconst_0
	iconst_1
	i2f
	fastore
	dup
	iconst_1
	iconst_2
	i2f
	fastore
	dup
	iconst_2
	iconst_3
	i2f
	fastore
	putstatic CodeGen/test/i [F
	
	; set limits used by this method
.limit locals 0
.limit stack 5
	return
.end method
	
	; standard constructor initializer 
.method public <init>()V
.limit stack 1
.limit locals 1
	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ LCodeGen/test; from L0 to L1
	new CodeGen/test
	dup
	invokenonvirtual CodeGen/test/<init>()V
	astore_1
.var 2 is a F from L0 to L1
	getstatic CodeGen/test/i [F
	iconst_1
	faload
	fstore_2
	getstatic CodeGen/test/i [F
	iconst_2
	fload_2
	fastore
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 3
.end method
