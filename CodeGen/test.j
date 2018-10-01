.class public CodeGen/test
.super java/lang/Object
	
.field static i I
.field static b [Z
	
	; standard class static initializer 
.method static <clinit>()V
	
	bipush 100
	putstatic CodeGen/test/i I
	dup
	iconst_0
	iconst_1
	bastore
	dup
	iconst_1
	iconst_0
	bastore
	putstatic CodeGen/test/b [Z
	
	; set limits used by this method
.limit locals 0
.limit stack 4
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
	getstatic CodeGen/test/i I
	invokestatic VC/lang/System/putIntLn(I)V
	getstatic CodeGen/test/b [Z
	iconst_0
	baload
	invokestatic VC/lang/System/putBoolLn(Z)V
	getstatic CodeGen/test/b [Z
	iconst_1
	baload
	invokestatic VC/lang/System/putBoolLn(Z)V
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 3
.end method
