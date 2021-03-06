package japgolly.scalajs.react.vdom

import japgolly.scalajs.react.{Ref, raw => Raw}
import scala.scalajs.js

class TagOf[+N <: TopNode] private[vdom](final val tag: String,
                                         final protected val modifiers: List[Seq[TagMod]],
                                         final val namespace: Namespace) extends VdomElement {

  def withRef[NN >: N <: TopNode, R](ref: TagOf.RefArg[NN]): TagOf[NN] =
    ref.value match {
      case Some(r) => this(Attr.Ref(r))
      case None    => this
    }

  def apply(xs: TagMod*): TagOf[N] =
    copy(modifiers = xs :: modifiers)

  protected def copy(tag: String = this.tag,
                     modifiers: List[Seq[TagMod]] = this.modifiers,
                     namespace: Namespace = this.namespace): TagOf[N] =
    new TagOf(tag, modifiers, namespace)

  override lazy val rawElement: Raw.React.Element = {
    val b = new Builder.ToRawReactElement()

    val arr = new js.Array[Seq[TagMod]]
    var current = modifiers
    while (current != Nil) {
      arr.push(current.head)
      current = current.tail
    }
    var j = arr.length
    while (j > 0) {
      j -= 1
      arr(j).foreach(_.applyTo(b))
    }

    b.render(tag)
  }

  override def applyTo(b: Builder): Unit =
    b.appendChild(rawElement)

  override def toString =
    if (modifiers.isEmpty)
      s"<$tag />"
    else
      s"<$tag>…</$tag>"

}

object TagOf {
  final case class RefArg[N <: TopNode](value: Option[Ref.Set[N]]) extends AnyVal
  object RefArg {
    implicit def set[N <: TopNode](r: Ref.Set[N]): RefArg[N] = apply(Some(r))
    implicit def option[N <: TopNode](o: Option[Ref.Set[N]]): RefArg[N] = apply(o)
  }
}

// =====================================================================================================================

final case class HtmlTagOf[+N <: HtmlTopNode](name: String) extends AnyVal {
  def apply(xs: TagMod*): TagOf[N] =
    new TagOf(name, xs :: Nil, Namespace.Html)
}

object HtmlTagOf {
  implicit def autoToTag[N <: HtmlTopNode](t: HtmlTagOf[N]): TagOf[N] =
    new TagOf[N](t.name, Nil, Namespace.Html)
}

// =====================================================================================================================

final case class SvgTagOf[+N <: SvgTopNode](name: String) extends AnyVal {
  def apply(xs: TagMod*): TagOf[N] =
    new TagOf(name, xs :: Nil, Namespace.Svg)
}

object SvgTagOf {
  implicit def autoToTag[N <: SvgTopNode](t: SvgTagOf[N]): TagOf[N] =
    new TagOf[N](t.name, Nil, Namespace.Svg)
}

