package chav1961.jcw.interfaces;

public enum TokenKeywords {
	undefined(TokenKeywordsSort.undefined),
	_abstract(TokenKeywordsSort.modifier),
	_assert(TokenKeywordsSort.statement),
	_boolean(TokenKeywordsSort.type),
	_break(TokenKeywordsSort.statement),
	_byte(TokenKeywordsSort.type),
	_case(TokenKeywordsSort.option),
	_catch(TokenKeywordsSort.option),
	_char(TokenKeywordsSort.type),
	_class(TokenKeywordsSort.type),
	_const(TokenKeywordsSort.reserved),
	_continue(TokenKeywordsSort.statement),
	_default(TokenKeywordsSort.option),
	_do(TokenKeywordsSort.statement),
	_double(TokenKeywordsSort.type),
	_else(TokenKeywordsSort.option),
	_enum(TokenKeywordsSort.type),
	_extends(TokenKeywordsSort.modifier),
	_false(TokenKeywordsSort.constant),
	_final(TokenKeywordsSort.modifier),
	_finally(TokenKeywordsSort.option),
	_float(TokenKeywordsSort.type),
	_for(TokenKeywordsSort.statement),
	_goto(TokenKeywordsSort.reserved),
	_if(TokenKeywordsSort.statement),
	_implements(TokenKeywordsSort.modifier),
	_import(TokenKeywordsSort.statement),
	_instanceof(TokenKeywordsSort.operator),
	_int(TokenKeywordsSort.type),
	_interface(TokenKeywordsSort.type),
	_long(TokenKeywordsSort.type),
	_native(TokenKeywordsSort.modifier),
	_new(TokenKeywordsSort.operator),
	_null(TokenKeywordsSort.constant),
	_package(TokenKeywordsSort.statement),
	_private(TokenKeywordsSort.visibility),
	_protected(TokenKeywordsSort.visibility),
	_public(TokenKeywordsSort.visibility),
	_return(TokenKeywordsSort.statement),
	_short(TokenKeywordsSort.type),
	_static(TokenKeywordsSort.modifier),
	_strictfp(TokenKeywordsSort.modifier),
	_super(TokenKeywordsSort.reference),
	_switch(TokenKeywordsSort.statement),
	_synchronized(TokenKeywordsSort.statement),
	_this(TokenKeywordsSort.reference),
	_throw(TokenKeywordsSort.statement),
	_throws(TokenKeywordsSort.modifier),
	_transient(TokenKeywordsSort.modifier),
	_true(TokenKeywordsSort.constant),
	_try(TokenKeywordsSort.statement),
	_void(TokenKeywordsSort.type),
	_volatile(TokenKeywordsSort.modifier),
	_while(TokenKeywordsSort.statement);
	
	private final TokenKeywordsSort	sort;
	
	private TokenKeywords(final TokenKeywordsSort sort) {
		this.sort = sort;
	}
	
	public TokenKeywordsSort getSort() {
		return sort;
	}
}
