/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.implcore.tags;

import io.opencensus.common.Scope;
import io.opencensus.tags.Internal;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.Tagger;
import java.util.Iterator;

public final class TaggerImpl extends Tagger {
  // All methods in this class use TagContextImpl and TagContextBuilderImpl. For example,
  // withTagContext(...) always puts a TagContextImpl into scope, even if the argument is another
  // TagContext subclass.
  //
  // TODO(sebright): Consider treating an unknown TagContext as empty.  That would allow us to
  // remove TagContext.unsafeGetIterator().

  @Override
  public TagContextImpl empty() {
    return TagContextImpl.EMPTY;
  }

  @Override
  public TagContextImpl getCurrentTagContext() {
    return toTagContextImpl(CurrentTagContextUtils.getCurrentTagContext());
  }

  @Override
  public TagContextBuilderImpl emptyBuilder() {
    return new TagContextBuilderImpl();
  }

  @Override
  public TagContextBuilder currentBuilder() {
    return toBuilder(CurrentTagContextUtils.getCurrentTagContext());
  }

  @Override
  public TagContextBuilderImpl toBuilder(TagContext tags) {
    return toTagContextBuilderImpl(tags);
  }

  @Override
  public Scope withTagContext(TagContext tags) {
    return CurrentTagContextUtils.withTagContext(toTagContextImpl(tags));
  }

  private static TagContextImpl toTagContextImpl(TagContext tags) {
    if (tags instanceof TagContextImpl) {
      return (TagContextImpl) tags;
    } else {
      Iterator<Tag> i = Internal.getTags(tags);
      if (!i.hasNext()) {
        return TagContextImpl.EMPTY;
      }
      TagContextBuilderImpl builder = new TagContextBuilderImpl();
      while (i.hasNext()) {
        Tag tag = i.next();
        if (tag != null) {
          TagContextUtils.addTagToBuilder(tag, builder);
        }
      }
      return builder.build();
    }
  }

  private static TagContextBuilderImpl toTagContextBuilderImpl(TagContext tags) {
    // Copy the tags more efficiently in the expected case, when the TagContext is a TagContextImpl.
    if (tags instanceof TagContextImpl) {
      return new TagContextBuilderImpl(((TagContextImpl) tags).getTags());
    } else {
      TagContextBuilderImpl builder = new TagContextBuilderImpl();
      for (Iterator<Tag> i = Internal.getTags(tags); i.hasNext(); ) {
        Tag tag = i.next();
        if (tag != null) {
          TagContextUtils.addTagToBuilder(tag, builder);
        }
      }
      return builder;
    }
  }
}
